package com.bgaidos.booking.api.room;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record RoomPatchRequest(
    UUID buildingId,
    String name,
    @Positive Integer capacity,
    String imageUrl,
    @Pattern(regexp = "male|female") String allowedGender,
    @PositiveOrZero Integer minAge,
    @PositiveOrZero Integer maxAge,
    Boolean leaderRoom
) {
}
