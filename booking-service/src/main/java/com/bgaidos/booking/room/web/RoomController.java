package com.bgaidos.booking.room.web;

import com.bgaidos.booking.api.room.RoomCreateRequest;
import com.bgaidos.booking.api.room.RoomPatchRequest;
import com.bgaidos.booking.api.room.RoomResponse;
import com.bgaidos.booking.room.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAuthority('rooms:read')")
    public List<RoomResponse> list(
        @RequestParam @Pattern(regexp = "male|female", message = "must be 'male' or 'female'") String gender,
        @RequestParam @Min(0) int age,
        @RequestParam(required = false) UUID buildingId
    ) {
        return roomService.list(gender, age, buildingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('rooms:write')")
    public RoomResponse create(@Valid @RequestBody RoomCreateRequest request) {
        return roomService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('rooms:write')")
    public RoomResponse patch(@PathVariable UUID id, @Valid @RequestBody RoomPatchRequest request) {
        return roomService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('rooms:write')")
    public void delete(@PathVariable UUID id) {
        roomService.delete(id);
    }
}
