package com.bgaidos.booking.api.room;

import java.time.Instant;
import java.util.UUID;

public record RoomHoldResponse(
    UUID id,
    UUID roomId,
    UUID camperId,
    Instant expiresAt
) {
}
