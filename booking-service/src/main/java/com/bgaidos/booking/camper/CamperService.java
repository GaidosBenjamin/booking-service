package com.bgaidos.booking.camper;

import com.bgaidos.booking.api.camper.CamperCreateRequest;
import com.bgaidos.booking.api.camper.CamperPatchRequest;
import com.bgaidos.booking.api.camper.CamperResponse;
import com.bgaidos.booking.api.camper.RoomAssignmentSummary;
import com.bgaidos.booking.api.camper.RoomHoldSummary;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.BadRequestException;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.Camper;
import com.bgaidos.booking.entity.CamperStatus;
import com.bgaidos.booking.entity.PaymentStatus;
import com.bgaidos.booking.entity.RoomAssignment;
import com.bgaidos.booking.entity.RoomHold;
import com.bgaidos.booking.payments.StripeRefundService;
import com.bgaidos.booking.repo.BookingItemRepository;
import com.bgaidos.booking.repo.CamperRepository;
import com.bgaidos.booking.repo.RoomAssignmentRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import com.bgaidos.booking.repo.RoomRepository;
import com.bgaidos.booking.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CamperService {

    /** Stand-in DOB for age-based room filtering; not collected in the registration UI. */
    private static final LocalDate DEFAULT_DATE_OF_BIRTH = LocalDate.of(2016, 6, 1);

    private final CamperRepository camperRepository;
    private final RoomHoldRepository holdRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final RoomRepository roomRepository;
    private final BookingItemRepository bookingItemRepository;
    private final UserRepository userRepository;
    private final CamperMapper mapper;
    private final CurrentUser currentUser;
    private final StripeRefundService stripeRefundService;

    @Transactional(readOnly = true)
    public List<CamperResponse> list() {
        var campers = camperRepository.findAllForCurrentUser();
        log.debug("list campers user={} count={}", currentUser.userId(), campers.size());

        var now = Instant.now();
        var camperIds = campers.stream().map(Camper::getId).toList();
        var holdByCamperId = holdRepository.findActiveForCurrentUser(now).stream()
            .collect(Collectors.toMap(h -> h.getCamper().getId(), h -> h));
        var assignmentByCamperId = assignmentRepository.findByCamperIds(camperIds).stream()
            .collect(Collectors.toMap(a -> a.getCamper().getId(), a -> a));

        return campers.stream()
            .map(camper -> mapper.toResponse(
                camper,
                computeStatus(camper, holdByCamperId.containsKey(camper.getId())).name(),
                hasRoomsAvailable(camper, now),
                toHoldSummary(holdByCamperId.get(camper.getId())),
                toAssignmentSummary(assignmentByCamperId.get(camper.getId()))))
            .toList();
    }

    public CamperResponse create(CamperCreateRequest request) {
        var camper = mapper.toEntity(request);
        camper.setDateOfBirth(DEFAULT_DATE_OF_BIRTH);
        camper.setTenantId(currentUser.tenantId());
        camper.setParentUser(userRepository.getReferenceById(currentUser.userId()));
        camper.setStatus(CamperStatus.NEEDS_BED);
        var saved = camperRepository.save(camper);
        log.info(
            "created camper id={} tenant={} parentUser={} firstName={} lastName={} grade={} gender={} dateOfBirth={} status={}",
            saved.getId(),
            saved.getTenantId(),
            currentUser.userId(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getGrade(),
            saved.getGender(),
            saved.getDateOfBirth(),
            saved.getStatus());
        return toResponseWithStatus(saved);
    }

    public CamperResponse patch(UUID id, CamperPatchRequest request) {
        var camper = camperRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("camper not found: " + id));
        mapper.applyPatch(request, camper);
        log.info("patched camper id={} user={}", id, currentUser.userId());
        return toResponseWithStatus(camper);
    }

    public void forceDelete(UUID id) {
        var camper = camperRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("camper not found: " + id));

        if (camper.getStatus() == CamperStatus.PAYMENT_SUCCESS) {
            var items = bookingItemRepository.findSucceededByCamperId(camper.getId(), PaymentStatus.SUCCEEDED);
            for (var item : items) {
                var booking = item.getBooking();
                if (booking.getStripePaymentIntentId() != null) {
                    stripeRefundService.refund(
                        booking.getStripePaymentIntentId(), item.getPrice(), booking.getCurrency());
                    booking.setStatus(PaymentStatus.REFUNDED);
                } else {
                    log.warn("skipping refund: no payment_intent_id on booking={} camper={}", booking.getId(), id);
                }
            }
        }

        assignmentRepository.deleteByCamperId(camper.getId());
        bookingItemRepository.deleteAllByCamperId(camper.getId());
        holdRepository.deleteByCamperId(camper.getId(), currentUser.tenantId());
        camperRepository.delete(camper);
        log.info("force-deleted camper id={} user={}", id, currentUser.userId());
    }

    public void delete(UUID id) {
        var camper = camperRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("camper not found: " + id));
        if (assignmentRepository.findByCamperId(camper.getId()).isPresent()) {
            throw new BadRequestException("camper has a room assignment — cannot delete");
        }
        if (bookingItemRepository.countByCamperIdAndBookingStatus(camper.getId(), PaymentStatus.PENDING) > 0) {
            throw new BadRequestException("camper has a pending booking — cannot delete");
        }
        bookingItemRepository.deleteDeadItemsByCamperId(camper.getId());
        holdRepository.deleteByCamperId(camper.getId(), currentUser.tenantId());
        camperRepository.delete(camper);
        log.info("deleted camper id={} user={}", id, currentUser.userId());
    }

    private CamperResponse toResponseWithStatus(Camper camper) {
        var now = Instant.now();
        var hold = holdRepository.findByCamperId(camper.getId()).orElse(null);
        var assignment = assignmentRepository.findByCamperId(camper.getId()).orElse(null);
        return mapper.toResponse(
            camper,
            computeStatus(camper, hold != null).name(),
            hasRoomsAvailable(camper, now),
            toHoldSummary(hold),
            toAssignmentSummary(assignment));
    }

    private static RoomHoldSummary toHoldSummary(RoomHold hold) {
        if (hold == null) return null;
        var room = hold.getRoom();
        return new RoomHoldSummary(
            hold.getId(),
            room.getName(),
            room.getImageUrl(),
            room.getBuilding().getName(),
            hold.getExpiresAt());
    }

    private static RoomAssignmentSummary toAssignmentSummary(RoomAssignment assignment) {
        if (assignment == null) return null;
        var room = assignment.getRoom();
        return new RoomAssignmentSummary(
            assignment.getId(),
            room.getName(),
            room.getImageUrl(),
            room.getBuilding().getName(),
            assignment.getAssignedOn());
    }

    private boolean hasRoomsAvailable(Camper camper, Instant now) {
        if (camper.getDateOfBirth() == null || camper.getGender() == null) {
            return false;
        }
        int age = Period.between(camper.getDateOfBirth(), LocalDate.now()).getYears();
        return roomRepository.hasAvailableRooms(camper.getGender(), age, now);
    }

    private static CamperStatus computeStatus(Camper camper, boolean hasActiveHold) {
        var stored = camper.getStatus();
        if (stored == CamperStatus.PAYMENT_SUCCESS || stored == CamperStatus.PAYMENT_FAILED) {
            return stored;
        }
        return hasActiveHold ? CamperStatus.NEEDS_PAYMENT : CamperStatus.NEEDS_BED;
    }
}
