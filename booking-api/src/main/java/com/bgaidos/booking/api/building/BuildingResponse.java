package com.bgaidos.booking.api.building;

import com.bgaidos.booking.api.tier.TierResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BuildingResponse(
    UUID id,
    String name,
    String description,
    List<String> highlights,
    String imageUrl,
    TierResponse tier,
    boolean isFull,
    Instant createdOn
) {
}
