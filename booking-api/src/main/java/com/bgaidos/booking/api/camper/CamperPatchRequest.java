package com.bgaidos.booking.api.camper;

import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CamperPatchRequest(
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    @Pattern(regexp = "GM[1-9]") String grade,
    @Pattern(regexp = "male|female") String gender,
    String specialRequirements
) {
}
