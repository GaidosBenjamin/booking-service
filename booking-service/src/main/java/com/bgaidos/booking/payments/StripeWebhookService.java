package com.bgaidos.booking.payments;

import com.bgaidos.booking.auth.service.event.BookingConfirmedEvent;
import com.bgaidos.booking.config.StripeConfig;
import com.bgaidos.booking.entity.Booking;
import com.bgaidos.booking.entity.BookingItem;
import com.bgaidos.booking.entity.CamperStatus;
import com.bgaidos.booking.entity.PaymentStatus;
import com.bgaidos.booking.entity.RoomAssignment;
import com.bgaidos.booking.repo.BookingItemRepository;
import com.bgaidos.booking.repo.BookingRepository;
import com.bgaidos.booking.repo.RoomAssignmentRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final RoomHoldRepository holdRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void handle(byte[] rawPayload, String signature) throws SignatureVerificationException {
        var event = Webhook.constructEvent(
            new String(rawPayload, StandardCharsets.UTF_8),
            signature,
            stripeConfig.getWebhookSecret());

        log.info("stripe webhook received type={}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed",
                 "checkout.session.async_payment_succeeded" -> {
                var session = extractSession(event);
                if (session != null) onSucceeded(session);
            }
            case "checkout.session.expired",
                 "checkout.session.async_payment_failed" -> {
                var session = extractSession(event);
                if (session != null) onExpiredOrFailed(event.getType(), session);
            }
            default -> log.warn("unhandled stripe event type={}", event.getType());
        }
    }

    private void onSucceeded(Session session) {
        // Atomic CAS update — only one concurrent webhook wins; prevents duplicate assignment creation
        var updated = bookingRepository.updateStatus(session.getId(), PaymentStatus.PENDING, PaymentStatus.SUCCEEDED);
        if (updated == 0) {
            var booking = bookingRepository.findByStripeSessionId(session.getId()).orElse(null);
            if (booking == null) {
                log.warn("checkout.session completed: no booking found for sessionId={}", session.getId());
            } else {
                log.debug("checkout.session idempotent: bookingId={} already {}", booking.getId(), booking.getStatus());
            }
            return;
        }
        var booking = bookingRepository.findByStripeSessionId(session.getId()).orElseThrow();
        var items = bookingItemRepository.findAllByBookingId(booking.getId());
        var now = Instant.now();
        items.forEach(item -> applyBookingItem(item, booking.getTenantId(), now));
        log.info("booking SUCCEEDED id={} sessionId={} assignments={}", booking.getId(), session.getId(), items.size());
        publishConfirmation(booking, items);
    }

    private void applyBookingItem(BookingItem item, UUID tenantId, Instant now) {
        holdRepository.deleteByCamperId(item.getCamper().getId(), tenantId);
        item.getCamper().setStatus(CamperStatus.PAYMENT_SUCCESS);
        var assignment = new RoomAssignment();
        assignment.setTenantId(tenantId);
        assignment.setRoom(item.getRoom());
        assignment.setCamper(item.getCamper());
        assignment.setAssignedOn(now);
        assignmentRepository.save(assignment);
    }

    private void publishConfirmation(Booking booking, List<BookingItem> items) {
        var camperNames = items.stream()
            .map(i -> i.getCamper().getFirstName() + " " + i.getCamper().getLastName())
            .toList();
        eventPublisher.publishEvent(new BookingConfirmedEvent(
            booking.getParentUser().getEmail(),
            booking.getId(),
            booking.getAmountTotal(),
            booking.getCurrency(),
            camperNames));
    }

    private void onExpiredOrFailed(String eventType, Session session) {
        bookingRepository.findByStripeSessionId(session.getId()).ifPresent(b -> {
            var newStatus = "checkout.session.expired".equals(eventType)
                ? PaymentStatus.CANCELED
                : PaymentStatus.FAILED;
            b.setStatus(newStatus);
            log.info("booking {} id={} sessionId={}", newStatus, b.getId(), session.getId());

            if (newStatus == PaymentStatus.FAILED) {
                var items = bookingItemRepository.findAllByBookingId(b.getId());
                items.forEach(item -> item.getCamper().setStatus(CamperStatus.PAYMENT_FAILED));
            }
        });
    }

    private static Session extractSession(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()
            && deserializer.getObject().get() instanceof Session s) {
            return s;
        }
        try {
            var obj = deserializer.deserializeUnsafe();
            log.debug("deserialized unsafe obj");
            if (obj instanceof Session s) return s;
        } catch (Exception e) {
            log.warn("could not deserialize Session from event type={}: {}", event.getType(), e.getMessage());
            return null;
        }
        log.warn("could not deserialize Session from event type={}", event.getType());
        return null;
    }
}

