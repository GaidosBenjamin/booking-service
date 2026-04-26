package com.bgaidos.booking.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
	@NotBlank String organizationSlug,
	@NotBlank @Email String email,
	@NotBlank String password,
	@NotBlank String firstName,
	@NotBlank String lastName,
	@NotBlank String phone) {
}
