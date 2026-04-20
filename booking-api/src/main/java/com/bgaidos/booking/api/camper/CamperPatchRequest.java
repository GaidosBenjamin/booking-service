package com.bgaidos.booking.api.camper;

import java.time.LocalDate;

public record CamperPatchRequest(
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String grade,
    String gender,
    String specialRequirements
) {
}
