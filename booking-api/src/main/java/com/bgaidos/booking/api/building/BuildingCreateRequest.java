package com.bgaidos.booking.api.building;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BuildingCreateRequest(
    @NotBlank String name,
    String description,
    List<@NotBlank String> highlights,
    @NotBlank String imageUrl,
    @NotNull UUID tierId
) {
}
