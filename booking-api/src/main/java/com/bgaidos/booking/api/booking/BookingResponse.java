package com.bgaidos.booking.api.booking;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    BigDecimal amountTotal,
    String currency,
    String status,
    String checkoutUrl,
    List<BookingItemResponse> items
) {
}
