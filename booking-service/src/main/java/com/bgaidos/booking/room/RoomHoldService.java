package com.bgaidos.booking.room;

import com.bgaidos.booking.api.room.RoomHoldCreateRequest;
import com.bgaidos.booking.api.room.RoomHoldResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.BadRequestException;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.Camper;
import com.bgaidos.booking.entity.Room;
import com.bgaidos.booking.entity.RoomHold;
import com.bgaidos.booking.repo.CamperRepository;
import com.bgaidos.booking.repo.RoomAssignmentRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import com.bgaidos.booking.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomHoldService {

    private final RoomHoldRepository holdRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final RoomRepository roomRepository;
    private final CamperRepository camperRepository;
    private final RoomHoldMapper mapper;
    private final CurrentUser currentUser;

    @Value("${app.room-hold.ttl}")
    private Duration ttl;

    public void delete(UUID id) {
        var hold = holdRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("hold not found: " + id));
        holdRepository.delete(hold);
        log.info("deleted room hold id={} camperId={}", id, hold.getCamper().getId());
    }

    @Transactional(readOnly = true)
    public List<RoomHoldResponse> list() {
        var holds = holdRepository.findActiveForCurrentUser(Instant.now());
        log.debug("list room holds user={} count={}", currentUser.userId(), holds.size());
        return holds.stream().map(mapper::toResponse).toList();
    }

    public RoomHoldResponse create(UUID roomId, RoomHoldCreateRequest request) {
        var camper = camperRepository.findByIdForCurrentUser(request.camperId())
            .orElseThrow(() -> new NotFoundException("camper not found: " + request.camperId()));

        if (assignmentRepository.findByCamperId(camper.getId()).isPresent()) {
            throw new BadRequestException("camper already assigned");
        }

        var room = roomRepository.findByIdForCurrentTenantForUpdate(roomId)
            .orElseThrow(() -> new NotFoundException("room not found: " + roomId));

        assertCompatible(camper, room);

        var existing = holdRepository.findByCamperId(camper.getId()).orElse(null);
        var now = Instant.now();

        if (existing == null || !room.getId().equals(existing.getRoom().getId())) {
            assertRoomHasCapacity(room, now);
        }

        RoomHold hold;
        if (existing != null) {
            hold = existing;
            hold.setRoom(room);
        } else {
            hold = new RoomHold();
            hold.setTenantId(currentUser.tenantId());
            hold.setCamper(camper);
            hold.setRoom(room);
        }
        hold.setExpiresAt(now.plus(ttl));

        var saved = holdRepository.save(hold);
        log.info("upserted room hold id={} roomId={} camperId={}",
            saved.getId(), room.getId(), camper.getId());
        return mapper.toResponse(saved);
    }

    private static void assertCompatible(Camper camper, Room room) {
        if (room.getAllowedGender() != null && !room.getAllowedGender().equals(camper.getGender())) {
            throw new BadRequestException("room does not allow camper's gender");
        }
        var dob = camper.getDateOfBirth();
        if (dob != null && (room.getMinAge() != null || room.getMaxAge() != null)) {
            int age = Period.between(dob, LocalDate.now()).getYears();
            if (room.getMinAge() != null && age < room.getMinAge()) {
                throw new BadRequestException("camper is too young for this room");
            }
            if (room.getMaxAge() != null && age > room.getMaxAge()) {
                throw new BadRequestException("camper is too old for this room");
            }
        }
    }

    private void assertRoomHasCapacity(Room room, Instant now) {
        var activeHolds = holdRepository.countActiveByRoomId(room.getId(), now);
        var assignments = assignmentRepository.countByRoomId(room.getId());
        if (activeHolds + assignments >= room.getCapacity()) {
            throw new BadRequestException("room is full");
        }
    }
}
