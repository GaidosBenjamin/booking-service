package com.bgaidos.booking.room.web;

import com.bgaidos.booking.api.room.RoomAssignmentCreateRequest;
import com.bgaidos.booking.api.room.RoomAssignmentPatchRequest;
import com.bgaidos.booking.api.room.RoomAssignmentResponse;
import com.bgaidos.booking.room.RoomAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms/assignments")
@RequiredArgsConstructor
public class RoomAssignmentController {

    private final RoomAssignmentService roomAssignmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('rooms:assignments:read')")
    public List<RoomAssignmentResponse> list() {
        return roomAssignmentService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('rooms:assignments:write')")
    public RoomAssignmentResponse create(@Valid @RequestBody RoomAssignmentCreateRequest request) {
        return roomAssignmentService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('rooms:assignments:write')")
    public RoomAssignmentResponse patch(@PathVariable UUID id, @Valid @RequestBody RoomAssignmentPatchRequest request) {
        return roomAssignmentService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('rooms:assignments:write')")
    public void delete(@PathVariable UUID id) {
        roomAssignmentService.delete(id);
    }
}
