package com.bgaidos.booking.room.web;

import com.bgaidos.booking.api.room.RoomHoldCreateRequest;
import com.bgaidos.booking.api.room.RoomHoldResponse;
import com.bgaidos.booking.room.RoomHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomHoldController {

    private final RoomHoldService roomHoldService;

    @GetMapping("/holds")
    @PreAuthorize("hasAuthority('rooms:holds:write')")
    public List<RoomHoldResponse> list() {
        return roomHoldService.list();
    }

    @PostMapping("/{roomId}/holds")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('rooms:holds:write')")
    public RoomHoldResponse upsert(@PathVariable UUID roomId, @Valid @RequestBody RoomHoldCreateRequest request) {
        return roomHoldService.create(roomId, request);
    }

    @DeleteMapping("/holds/{holdId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('rooms:holds:write')")
    public void delete(@PathVariable UUID holdId) {
        roomHoldService.delete(holdId);
    }
}
