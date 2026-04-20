package com.bgaidos.booking.api.auth;

import java.util.UUID;

public record AuthTokenResponse(
	String accessToken,
	String tokenType,
	long expiresIn,
	String refreshToken,
	long refreshExpiresIn,
	UUID userId,
	UUID tenantId
) {
}
