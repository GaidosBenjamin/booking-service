package com.bgaidos.booking.api.donation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DonationResponse(
	UUID id,
	BigDecimal amount,
	String currency,
	String status,
	String checkoutUrl,
	Instant expiresAt
) {}
