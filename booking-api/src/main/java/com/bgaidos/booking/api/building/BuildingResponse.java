package com.bgaidos.booking.api.building;

import com.bgaidos.booking.api.tier.TierResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BuildingResponse(
    UUID id,
    String name,
    Map<String, String> description,
    Map<String, List<HighlightItemDto>> highlights,
    String imageUrl,
    TierResponse tier,
    boolean isFull,
    Instant createdOn
) {
}
