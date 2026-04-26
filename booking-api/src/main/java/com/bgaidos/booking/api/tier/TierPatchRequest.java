package com.bgaidos.booking.api.tier;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TierPatchRequest(
    String name,
    String description,
    @PositiveOrZero BigDecimal basePrice,
    @PositiveOrZero BigDecimal discountPrice,
    @Pattern(regexp = "[A-Z]{3}") String currency
) {
}
