package com.bgaidos.booking.payments;

import com.bgaidos.booking.api.booking.BookingCreateRequest;
import com.bgaidos.booking.api.booking.BookingResponse;
import com.bgaidos.booking.config.StripeConfig;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.BadRequestException;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.Booking;
import com.bgaidos.booking.entity.BookingItem;
import com.bgaidos.booking.entity.PaymentStatus;
import com.bgaidos.booking.entity.RoomHold;
import com.bgaidos.booking.repo.BookingItemRepository;
import com.bgaidos.booking.repo.BookingRepository;
import com.bgaidos.booking.repo.CamperRepository;
import com.bgaidos.booking.repo.MemberRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import com.bgaidos.booking.repo.RoomRepository;
import com.bgaidos.booking.repo.TierRepository;
import com.bgaidos.booking.repo.UserProfileRepository;
import com.bgaidos.booking.repo.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final RoomHoldRepository holdRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MemberRepository memberRepository;
    private final CamperRepository camperRepository;
    private final TierRepository tierRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper mapper;
    private final CurrentUser currentUser;
    private final StripeConfig stripeConfig;
    private final PlatformTransactionManager txManager;
    private final BookingConfirmationService confirmationService;

    public BookingResponse create(BookingCreateRequest request) {
        var readTx = new TransactionTemplate(txManager);
        readTx.setReadOnly(true);
        List<PreparedItem> prepared = Objects.requireNonNull(readTx.execute(status -> {
            var holds = resolveHolds(request);
            var camperIds = holds.stream().map(h -> h.getCamper().getId()).toList();
            var alreadyBooked = bookingItemRepository.findAlreadyBookedCamperIds(
                camperIds, List.of(PaymentStatus.PENDING, PaymentStatus.SUCCEEDED));
            if (!alreadyBooked.isEmpty()) {
                throw new BadRequestException("camper(s) already have an active booking: " + alreadyBooked);
            }
            var isMember = isMember(holds);
            var tenantId = currentUser.tenantId();
            return holds.stream().map(hold -> {
                var tier = hold.getRoom().getBuilding().getTier();
                return new PreparedItem(
                    tenantId,
                    hold.getCamper().getId(),
                    hold.getCamper().getFirstName(),
                    hold.getCamper().getLastName(),
                    tier.getId(),
                    tier.getName(),
                    tier.getCurrency(),
                    hold.getRoom().getId(),
                    isMember ? tier.getDiscountPrice() : tier.getBasePrice()
                );
            }).toList();
        }));

        var currency = prepared.getFirst().tierCurrency().toLowerCase();
        var total = prepared.stream().map(PreparedItem::price).reduce(BigDecimal.ZERO, BigDecimal::add);

        var b = new Booking();
        b.setId();

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return createFreeBooking(b, prepared, currency, total);
        }

        var stripeLineItems = prepared.stream()
            .filter(item -> item.price().compareTo(BigDecimal.ZERO) > 0)
            .map(item -> toStripeLineItem(item.camperFirstName(), item.camperLastName(), item.tierName(), item.price(), currency))
            .toList();
        if (stripeLineItems.isEmpty()) {
            throw new BadRequestException("no paid items to checkout");
        }
        var session = createCheckoutSession(stripeLineItems, currency, b.getId());

        var writeTx = new TransactionTemplate(txManager);
        var booking = Objects.requireNonNull(writeTx.execute(status -> {
            b.setTenantId(currentUser.tenantId());
            b.setParentUser(userRepository.getReferenceById(currentUser.userId()));
            b.setStripeSessionId(session.getId());
            b.setAmountTotal(total);
            b.setCurrency(currency.toUpperCase());
            b.setStatus(PaymentStatus.PENDING);
            var sessionExpiresAt = Instant.ofEpochSecond(session.getExpiresAt());
            b.setExpiresAt(sessionExpiresAt);
            var saved = bookingRepository.save(b);
            var camperIds = prepared.stream().map(PreparedItem::camperId).toList();
            holdRepository.extendByCamperIds(camperIds, currentUser.tenantId(), sessionExpiresAt);
            persistItems(saved, prepared);
            return saved;
        }));

        log.info("created booking id={} sessionId={} amount={} campers={}",
            booking.getId(), session.getId(), total, prepared.size());
        return toResponse(booking, session.getUrl());
    }

    private BookingResponse createFreeBooking(
        Booking b,
        List<PreparedItem> prepared,
        String currency,
        BigDecimal total
    ) {
        var expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);
        var writeTx = new TransactionTemplate(txManager);
        var booking = Objects.requireNonNull(writeTx.execute(status -> {
            b.setTenantId(currentUser.tenantId());
            b.setParentUser(userRepository.getReferenceById(currentUser.userId()));
            b.setAmountTotal(total);
            b.setCurrency(currency.toUpperCase());
            b.setStatus(PaymentStatus.PENDING);
            b.setExpiresAt(expiresAt);
            var saved = bookingRepository.save(b);
            var camperIds = prepared.stream().map(PreparedItem::camperId).toList();
            holdRepository.extendByCamperIds(camperIds, currentUser.tenantId(), expiresAt);
            persistItems(saved, prepared);
            return saved;
        }));

        log.info("created free pending booking id={} amount={} campers={}", booking.getId(), total, prepared.size());
        return toResponse(booking, null);
    }

    @Transactional
    public BookingResponse confirm(UUID id) {
        var booking = bookingRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("booking not found: " + id));
        if (booking.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("booking is not pending");
        }
        if (booking.getAmountTotal().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("only free bookings can be confirmed without payment");
        }
        var items = bookingItemRepository.findAllByBookingId(id);
        if (items.isEmpty()) {
            throw new BadRequestException("booking has no items");
        }
        confirmationService.confirmAllItems(booking, items);
        booking.setStatus(PaymentStatus.SUCCEEDED);
        log.info("confirmed free booking id={} campers={}", id, items.size());
        return toResponse(booking, null);
    }

    private void persistItems(Booking booking, List<PreparedItem> prepared) {
        for (var item : prepared) {
            var bi = new BookingItem();
            bi.setTenantId(item.tenantId());
            bi.setBooking(booking);
            bi.setCamper(camperRepository.getReferenceById(item.camperId()));
            bi.setTier(tierRepository.getReferenceById(item.tierId()));
            bi.setRoom(roomRepository.getReferenceById(item.roomId()));
            bi.setPrice(item.price());
            bookingItemRepository.save(bi);
        }
    }

    @Transactional
    public void cancel(UUID id) {
        var booking = bookingRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("booking not found: " + id));
        if (booking.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new BadRequestException("cannot cancel a succeeded booking");
        }
        if (booking.getStatus() == PaymentStatus.CANCELED) {
            return;
        }
        booking.setStatus(PaymentStatus.CANCELED);
        if (booking.getStripeSessionId() != null) {
            expireStripeSession(booking.getStripeSessionId());
        }
        var camperIds = bookingItemRepository.findAllByBookingId(id).stream()
            .map(item -> item.getCamper().getId())
            .toList();
        if (!camperIds.isEmpty()) {
            holdRepository.resetActiveByCamperIds(
                camperIds, booking.getTenantId(), Instant.now().plus(15, ChronoUnit.MINUTES));
        }
        log.info("canceled booking id={}", id);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> list() {
        return bookingRepository.findAllForCurrentUser(List.of(PaymentStatus.PENDING, PaymentStatus.SUCCEEDED)).stream()
            .map(b -> toResponse(b, null))
            .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID id) {
        var booking = bookingRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("booking not found: " + id));
        return toResponse(booking, null);
    }

    private List<RoomHold> resolveHolds(BookingCreateRequest request) {
        var holds = holdRepository.findActiveForCurrentUser(Instant.now());
        if (request != null && request.camperIds() != null && !request.camperIds().isEmpty()) {
            var filter = request.camperIds();
            holds = holds.stream().filter(h -> filter.contains(h.getCamper().getId())).toList();
        }
        if (holds.isEmpty()) {
            throw new BadRequestException("no active holds found");
        }
        return holds;
    }

    private boolean isMember(List<RoomHold> holds) {
        var parentUser = holds.getFirst().getCamper().getParentUser();
        var email = parentUser.getEmail().toLowerCase();
        var phone = userProfileRepository.findByUserId(parentUser.getId())
            .map(p -> p.getPhone() != null ? p.getPhone().toLowerCase() : null)
            .orElse(null);
        return memberRepository.existsByTenantIdAndEmailOrPhone(
            currentUser.tenantId(), email, phone);
    }

    private Session createCheckoutSession(List<SessionCreateParams.LineItem> lineItems, String currency, UUID bookingId) {
        try {
            return Session.create(SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                    .setDescription(StripeEntityType.BOOKING.description)
                    .build())
                .setCustomerEmail(currentUser.email())
                .setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES).getEpochSecond())
                .setSuccessUrl(stripeConfig.getSuccessUrl() + "?bookingId=" + bookingId)
                .setCancelUrl(stripeConfig.getCancelUrl() + "?bookingId=" + bookingId)
                .addAllLineItem(lineItems)
                .putMetadata("entityType", StripeEntityType.BOOKING.name())
                .putMetadata("tenantId", currentUser.tenantId().toString())
                .putMetadata("parentUserId", currentUser.userId().toString())
                .build());
        } catch (StripeException ex) {
            throw new RuntimeException("Stripe Checkout Session creation failed: " + ex.getMessage(), ex);
        }
    }

    private BookingResponse toResponse(Booking booking, String checkoutUrl) {
        var items = bookingItemRepository.findAllByBookingId(booking.getId()).stream()
            .map(item -> {
                var hold = holdRepository.findByCamperId(item.getCamper().getId()).orElse(null);
                return mapper.toItemResponse(item, hold != null ? hold.getExpiresAt() : null);
            })
            .toList();
        return new BookingResponse(
            booking.getId(),
            booking.getAmountTotal(),
            booking.getCurrency(),
            booking.getStatus().name(),
            checkoutUrl,
            booking.getExpiresAt(),
            items);
    }

    private static SessionCreateParams.LineItem toStripeLineItem(
        String firstName, String lastName, String tierName, BigDecimal price, String currency
    ) {
        var priceData = SessionCreateParams.LineItem.PriceData.builder()
            .setCurrency(currency)
            .setUnitAmount(toLongCents(price))
            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(firstName + " " + lastName + " — " + tierName)
                .build())
            .build();
        return SessionCreateParams.LineItem.builder()
            .setPriceData(priceData)
            .setQuantity(1L)
            .build();
    }

    private static void expireStripeSession(String sessionId) {
        try {
            Session.retrieve(sessionId).expire();
        } catch (StripeException ex) {
            log.warn("could not expire stripe session sessionId={}: {}", sessionId, ex.getMessage());
        }
    }

    private static long toLongCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
    }

    private record PreparedItem(
        UUID tenantId,
        UUID camperId, String camperFirstName, String camperLastName,
        UUID tierId, String tierName, String tierCurrency,
        UUID roomId,
        BigDecimal price
    ) {}
}
