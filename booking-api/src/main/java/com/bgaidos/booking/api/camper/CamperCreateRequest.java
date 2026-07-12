package com.bgaidos.booking.api.camper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CamperCreateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Pattern(regexp = "GM[1-9]") String grade,
    @NotBlank @Pattern(regexp = "male|female") String gender,
    String specialRequirements
) {
}
