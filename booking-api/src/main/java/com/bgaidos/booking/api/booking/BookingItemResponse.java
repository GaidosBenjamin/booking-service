package com.bgaidos.booking.api.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingItemResponse(
    UUID roomId,
    BigDecimal price,
    Instant holdExpiresAt,
    TierSummary tier,
    CamperSummary camper
) {
    public record TierSummary(
        UUID id,
        String name,
        String currency,
        BigDecimal basePrice,
        BigDecimal discountedPrice
    ) {}

    public record CamperSummary(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String grade,
        String gender
    ) {}
}
