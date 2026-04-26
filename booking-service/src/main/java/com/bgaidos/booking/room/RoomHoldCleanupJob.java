package com.bgaidos.booking.room;

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

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void deleteExpiredHolds() {
        var deleted = holdRepository.deleteExpired();
        if (deleted > 0) {
            log.info("cleaned up {} expired room hold(s)", deleted);
        }
    }
}
