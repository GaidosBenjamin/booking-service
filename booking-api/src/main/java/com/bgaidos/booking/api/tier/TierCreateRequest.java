package com.bgaidos.booking.api.tier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TierCreateRequest(
    @NotBlank String name,
    String description,
    @NotNull @PositiveOrZero BigDecimal basePrice,
    @NotNull @PositiveOrZero BigDecimal discountPrice,
    @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency
) {
}
