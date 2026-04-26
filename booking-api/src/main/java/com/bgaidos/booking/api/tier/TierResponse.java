package com.bgaidos.booking.api.tier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TierResponse(
    UUID id,
    String name,
    String description,
    BigDecimal basePrice,
    BigDecimal discountPrice,
    String currency,
    boolean memberDiscount,
    Instant createdOn
) {
}
