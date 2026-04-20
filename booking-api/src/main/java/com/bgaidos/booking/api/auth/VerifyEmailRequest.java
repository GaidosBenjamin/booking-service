package com.bgaidos.booking.api.auth;

import com.bgaidos.booking.api.validation.SixDigitCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
    @NotBlank String organizationSlug,
    @NotBlank @Email String email,
    @SixDigitCode String code
) {
}
