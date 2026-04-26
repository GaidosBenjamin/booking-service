package com.bgaidos.booking.api.user;

import jakarta.validation.constraints.Pattern;

public record UserPatchRequest(
    String firstName,
    String lastName,
    @Pattern(regexp = "\\d{4} \\d{3} \\d{3}") String phone
) {
}
