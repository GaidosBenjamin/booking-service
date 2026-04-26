package com.bgaidos.booking.api.leader;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaderResponse(
    UUID id,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String gender,
    Instant createdOn
) {
}
