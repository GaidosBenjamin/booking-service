package com.bgaidos.booking.api.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record RoomCreateRequest(
    @NotNull UUID buildingId,
    @NotBlank String name,
    @NotNull @Positive Integer capacity,
    @NotBlank String imageUrl,
    @Pattern(regexp = "male|female") String allowedGender,
    @PositiveOrZero Integer minAge,
    @PositiveOrZero Integer maxAge,
    Boolean leaderRoom
) {
}
