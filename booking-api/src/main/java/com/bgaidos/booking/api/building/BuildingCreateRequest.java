package com.bgaidos.booking.api.building;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BuildingCreateRequest(
    @NotBlank String name,
    Map<String, String> description,
    Map<String, List<HighlightItemDto>> highlights,
    @NotBlank String imageUrl,
    @NotNull UUID tierId
) {
}
