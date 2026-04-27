package com.bgaidos.booking.room;

import com.bgaidos.booking.repo.BookingRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomHoldCleanupJob {

    private final RoomHoldRepository holdRepository;
    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void deleteExpiredHolds() {
        var canceled = bookingRepository.cancelExpiredPending();
        if (canceled > 0) {
            log.info("canceled {} expired pending booking(s)", canceled);
        }
        var deleted = holdRepository.deleteExpired();
        if (deleted > 0) {
            log.info("cleaned up {} expired room hold(s)", deleted);
        }
    }
}
