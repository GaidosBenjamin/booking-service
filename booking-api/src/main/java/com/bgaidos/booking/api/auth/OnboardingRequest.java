package com.bgaidos.booking.api.auth;

import jakarta.validation.constraints.NotBlank;

public record OnboardingRequest(
	@NotBlank String organizationName,
	@NotBlank String organizationSlug) {
}
