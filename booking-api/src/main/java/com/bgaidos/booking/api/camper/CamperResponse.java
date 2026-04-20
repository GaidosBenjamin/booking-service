package com.bgaidos.booking.api.camper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CamperResponse(
    UUID id,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String grade,
    String gender,
    String specialRequirements,
    String status,
    Instant createdOn
) {
}
