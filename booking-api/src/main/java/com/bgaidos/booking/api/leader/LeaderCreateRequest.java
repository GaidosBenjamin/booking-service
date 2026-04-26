package com.bgaidos.booking.api.leader;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record LeaderCreateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Past LocalDate dateOfBirth,
    @Pattern(regexp = "male|female") String gender
) {
}
