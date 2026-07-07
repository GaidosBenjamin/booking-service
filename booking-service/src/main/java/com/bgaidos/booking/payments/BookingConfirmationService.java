package com.bgaidos.booking.payments;

import com.bgaidos.booking.auth.service.event.BookingConfirmedEvent;
import com.bgaidos.booking.entity.Booking;
import com.bgaidos.booking.entity.BookingItem;
import com.bgaidos.booking.entity.CamperStatus;
import com.bgaidos.booking.entity.RoomAssignment;
import com.bgaidos.booking.repo.RoomAssignmentRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import com.bgaidos.booking.repo.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingConfirmationService {

    private final RoomHoldRepository holdRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final UserProfileRepository userProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void confirmAllItems(Booking booking, List<BookingItem> items) {
        var now = Instant.now();
        items.forEach(item -> confirmItem(item, booking.getTenantId(), now));
        publishConfirmation(booking, items);
    }

    public void confirmItem(BookingItem item, UUID tenantId, Instant now) {
        holdRepository.deleteByCamperId(item.getCamper().getId(), tenantId);
        item.getCamper().setStatus(CamperStatus.PAYMENT_SUCCESS);
        var assignment = new RoomAssignment();
        assignment.setTenantId(tenantId);
        assignment.setRoom(item.getRoom());
        assignment.setCamper(item.getCamper());
        assignment.setAssignedOn(now);
        assignmentRepository.save(assignment);
    }

    public void publishConfirmation(Booking booking, List<BookingItem> items) {
        var camperNames = items.stream()
            .map(i -> i.getCamper().getFirstName() + " " + i.getCamper().getLastName())
            .toList();
        var profile = userProfileRepository.findByUserId(booking.getParentUser().getId()).orElse(null);
        var language = profile != null ? profile.getPreferredLocale() : "ro";
        var phone = profile != null ? profile.getPhone() : null;
        eventPublisher.publishEvent(new BookingConfirmedEvent(
            booking.getParentUser().getEmail(),
            booking.getId(),
            booking.getAmountTotal(),
            booking.getCurrency(),
            camperNames,
            language,
            phone));
    }
}
