package com.bgaidos.booking.auth.service.event;

import java.math.BigDecimal;
import java.util.UUID;

public record DonationConfirmedEvent(
    String email,
    UUID donationId,
    BigDecimal amount,
    String currency,
    String donorName
) {
}
