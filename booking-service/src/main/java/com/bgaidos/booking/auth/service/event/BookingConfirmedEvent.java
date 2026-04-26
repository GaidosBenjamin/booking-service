package com.bgaidos.booking.auth.service.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BookingConfirmedEvent(
    String email,
    UUID bookingId,
    BigDecimal total,
    String currency,
    List<String> camperNames
) {
}
