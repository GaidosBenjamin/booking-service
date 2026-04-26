package com.bgaidos.booking.api.camper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CamperCreateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull LocalDate dateOfBirth,
    @NotBlank String grade,
    @NotBlank @Pattern(regexp = "male|female") String gender,
    String specialRequirements
) {
}
