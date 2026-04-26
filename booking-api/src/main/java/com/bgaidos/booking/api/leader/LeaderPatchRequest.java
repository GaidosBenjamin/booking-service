package com.bgaidos.booking.api.leader;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record LeaderPatchRequest(
    String firstName,
    String lastName,
    @Past LocalDate dateOfBirth,
    @Pattern(regexp = "male|female") String gender
) {
}
