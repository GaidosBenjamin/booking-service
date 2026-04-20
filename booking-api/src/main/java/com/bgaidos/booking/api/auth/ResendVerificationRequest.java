package com.bgaidos.booking.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(
    @NotBlank String organizationSlug,
    @NotBlank @Email String email
) {
}
