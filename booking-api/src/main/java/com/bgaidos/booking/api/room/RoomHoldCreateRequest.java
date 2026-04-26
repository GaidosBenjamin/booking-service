package com.bgaidos.booking.api.room;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RoomHoldCreateRequest(
    @NotNull UUID camperId
) {
}
