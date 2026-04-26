package com.bgaidos.booking.api.room;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RoomAssignmentCreateRequest(
    @NotNull UUID roomId,
    UUID camperId,
    UUID leaderId
) {
}
