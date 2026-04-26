package com.bgaidos.booking.api.camper;

import java.time.Instant;
import java.util.UUID;

public record RoomHoldSummary(
    UUID id,
    String name,
    String imageUrl,
    String buildingName,
    Instant holdExpiresAt
) {
}
