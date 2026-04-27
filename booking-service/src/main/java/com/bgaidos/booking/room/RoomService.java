package com.bgaidos.booking.room;

import com.bgaidos.booking.api.room.RoomCreateRequest;
import com.bgaidos.booking.api.room.RoomOccupantResponse;
import com.bgaidos.booking.api.room.RoomPatchRequest;
import com.bgaidos.booking.api.room.RoomResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.BadRequestException;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.Building;
import com.bgaidos.booking.entity.Room;
import com.bgaidos.booking.entity.RoomAssignment;
import com.bgaidos.booking.entity.RoomHold;
import com.bgaidos.booking.repo.BuildingRepository;
import com.bgaidos.booking.repo.RoomAssignmentRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import com.bgaidos.booking.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final RoomHoldRepository holdRepository;
    private final BuildingRepository buildingRepository;
    private final RoomMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<RoomResponse> list(String gender, int age, UUID buildingId) {
        var rooms = roomRepository.findForCurrentTenantFiltered(gender, age, buildingId);
        log.debug("list rooms tenant={} gender={} age={} buildingId={} count={}", currentUser.tenantId(), gender, age, buildingId, rooms.size());

        var roomIds = rooms.stream().map(Room::getId).toList();

        var assignmentsByRoom = assignmentRepository.findByRoomIds(roomIds).stream()
            .collect(Collectors.groupingBy(a -> a.getRoom().getId()));

        var holdsByRoom = holdRepository.findActiveByRoomIds(roomIds, Instant.now()).stream()
            .collect(Collectors.groupingBy(h -> h.getRoom().getId()));

        return rooms.stream()
            .map(room -> toRoomResponse(room, assignmentsByRoom, holdsByRoom))
            .toList();
    }

    public RoomResponse create(RoomCreateRequest request) {
        var room = mapper.toEntity(request);
        room.setTenantId(currentUser.tenantId());
        room.setBuilding(resolveBuilding(request.buildingId()));
        if (request.leaderRoom() == null) {
            room.setLeaderRoom(false);
        }
        assertAgeRange(room.getMinAge(), room.getMaxAge());
        var saved = roomRepository.save(room);
        log.info("created room id={} tenant={} user={}", saved.getId(), currentUser.tenantId(), currentUser.userId());
        return mapper.toResponse(saved);
    }

    public RoomResponse patch(UUID id, RoomPatchRequest request) {
        var room = roomRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("room not found: " + id));
        mapper.applyPatch(request, room);
        if (request.buildingId() != null) {
            room.setBuilding(resolveBuilding(request.buildingId()));
        }
        assertAgeRange(room.getMinAge(), room.getMaxAge());
        log.info("patched room id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
        return mapper.toResponse(room);
    }

    public void delete(UUID id) {
        var room = roomRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("room not found: " + id));
        roomRepository.delete(room);
        log.info("deleted room id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
    }

    private RoomResponse toRoomResponse(
        Room room,
        Map<UUID, List<RoomAssignment>> assignmentsByRoom,
        Map<UUID, List<RoomHold>> holdsByRoom
    ) {
        var base = mapper.toResponse(room);
        var assignments = assignmentsByRoom.getOrDefault(room.getId(), List.of()).stream()
            .map(RoomService::toOccupant)
            .toList();
        var holds = holdsByRoom.getOrDefault(room.getId(), List.of()).stream()
            .map(RoomService::toOccupant)
            .toList();
        return new RoomResponse(base.id(), base.name(), base.capacity(), base.imageUrl(), base.leaderRoom(), assignments, holds);
    }

    private static RoomOccupantResponse toOccupant(RoomAssignment a) {
        if (a.getCamper() != null) {
            return new RoomOccupantResponse(a.getCamper().getId(), a.getCamper().getFirstName(), a.getCamper().getLastName());
        }
        return new RoomOccupantResponse(a.getLeader().getId(), a.getLeader().getFirstName(), a.getLeader().getLastName());
    }

    private static RoomOccupantResponse toOccupant(RoomHold h) {
        return new RoomOccupantResponse(h.getCamper().getId(), h.getCamper().getFirstName(), h.getCamper().getLastName());
    }

    private Building resolveBuilding(UUID buildingId) {
        return buildingRepository.findByIdForCurrentTenant(buildingId)
            .orElseThrow(() -> new NotFoundException("building not found: " + buildingId));
    }

    private static void assertAgeRange(Integer minAge, Integer maxAge) {
        if (minAge == null || maxAge == null) {
            return;
        }
        if (maxAge < minAge) {
            throw new BadRequestException("maxAge must be greater than or equal to minAge");
        }
    }
}

