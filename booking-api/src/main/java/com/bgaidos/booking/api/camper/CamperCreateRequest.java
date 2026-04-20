package com.bgaidos.booking.api.camper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CamperCreateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull LocalDate dateOfBirth,
    @NotBlank String grade,
    @NotBlank String gender,
    String specialRequirements
) {
}
