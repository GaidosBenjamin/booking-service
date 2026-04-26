package com.bgaidos.booking.api.building;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record BuildingPatchRequest(
    String name,
    String description,
    List<@NotBlank String> highlights,
    String imageUrl,
    UUID tierId
) {
}
