package com.bgaidos.booking.room;

import com.bgaidos.booking.api.room.RoomAssignmentCreateRequest;
import com.bgaidos.booking.api.room.RoomAssignmentPatchRequest;
import com.bgaidos.booking.api.room.RoomAssignmentResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.BadRequestException;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.Room;
import com.bgaidos.booking.entity.RoomAssignment;
import com.bgaidos.booking.repo.CamperRepository;
import com.bgaidos.booking.repo.LeaderRepository;
import com.bgaidos.booking.repo.RoomAssignmentRepository;
import com.bgaidos.booking.repo.RoomHoldRepository;
import com.bgaidos.booking.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomAssignmentService {

    private final RoomAssignmentRepository assignmentRepository;
    private final RoomHoldRepository holdRepository;
    private final RoomRepository roomRepository;
    private final CamperRepository camperRepository;
    private final LeaderRepository leaderRepository;
    private final RoomAssignmentMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<RoomAssignmentResponse> list() {
        var assignments = assignmentRepository.findAllForCurrentTenant();
        log.debug("list room assignments tenant={} count={}", currentUser.tenantId(), assignments.size());
        return assignments.stream()
            .map(mapper::toResponse)
            .toList();
    }

    public RoomAssignmentResponse create(RoomAssignmentCreateRequest request) {
        assertXor(request.camperId(), request.leaderId());
        var room = roomRepository.findByIdForCurrentTenantForUpdate(request.roomId())
            .orElseThrow(() -> new NotFoundException("room not found: " + request.roomId()));
        assertRoomHasRoom(room, Instant.now());
        var assignment = new RoomAssignment();
        assignment.setTenantId(currentUser.tenantId());
        assignment.setRoom(room);
        assignment.setAssignedOn(Instant.now());
        populateOccupant(assignment, request, room);
        var saved = assignmentRepository.save(assignment);
        log.info("created room assignment id={} roomId={} camperId={} leaderId={}",
            saved.getId(), room.getId(),
            saved.getCamper() == null ? null : saved.getCamper().getId(),
            saved.getLeader() == null ? null : saved.getLeader().getId());
        return mapper.toResponse(saved);
    }

    public RoomAssignmentResponse patch(UUID id, RoomAssignmentPatchRequest request) {
        var assignment = assignmentRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("room assignment not found: " + id));

        if (request.roomId() != null && !request.roomId().equals(assignment.getRoom().getId())) {
            var newRoom = roomRepository.findByIdForCurrentTenantForUpdate(request.roomId())
                .orElseThrow(() -> new NotFoundException("room not found: " + request.roomId()));
            assertRoomHasRoom(newRoom, Instant.now());
            if (assignment.getLeader() != null && !newRoom.isLeaderRoom()) {
                throw new BadRequestException("leaders can only be assigned to leader rooms");
            }
            assignment.setRoom(newRoom);
        }

        log.info("patched room assignment id={} roomId={}", assignment.getId(), assignment.getRoom().getId());
        return mapper.toResponse(assignment);
    }

    public void delete(UUID id) {
        var assignment = assignmentRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("room assignment not found: " + id));
        assignmentRepository.delete(assignment);
        log.info("deleted room assignment id={}", id);
    }

    private void populateOccupant(RoomAssignment assignment, RoomAssignmentCreateRequest request, Room room) {
        if (request.camperId() != null) {
            var camper = camperRepository.findByIdForCurrentTenant(request.camperId())
                .orElseThrow(() -> new NotFoundException("camper not found: " + request.camperId()));
            assignment.setCamper(camper);
            holdRepository.deleteByCamperId(camper.getId(), currentUser.tenantId());
        } else {
            if (!room.isLeaderRoom()) {
                throw new BadRequestException("leaders can only be assigned to leader rooms");
            }
            var leader = leaderRepository.findByIdForCurrentTenant(request.leaderId())
                .orElseThrow(() -> new NotFoundException("leader not found: " + request.leaderId()));
            assignment.setLeader(leader);
        }
    }

    private static void assertXor(UUID camperId, UUID leaderId) {
        if ((camperId == null) == (leaderId == null)) {
            throw new BadRequestException("provide exactly one of camperId or leaderId");
        }
    }

    private void assertRoomHasRoom(Room room, Instant now) {
        var activeHolds = holdRepository.countActiveByRoomId(room.getId(), now);
        var assignments = assignmentRepository.countByRoomId(room.getId());
        if (activeHolds + assignments >= room.getCapacity()) {
            throw new BadRequestException("room is full");
        }
    }
}
