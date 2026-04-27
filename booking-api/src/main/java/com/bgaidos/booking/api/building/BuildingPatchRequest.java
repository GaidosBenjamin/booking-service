package com.bgaidos.booking.api.building;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BuildingPatchRequest(
    String name,
    Map<String, String> description,
    Map<String, List<HighlightItemDto>> highlights,
    String imageUrl,
    UUID tierId
) {
}
