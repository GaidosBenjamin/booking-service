package com.bgaidos.booking.api.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingItemResponse(
    UUID camperId,
    UUID tierId,
    UUID roomId,
    BigDecimal price,
    Instant holdExpiresAt
) {
}
