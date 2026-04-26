package com.bgaidos.booking.api.user;

import java.util.UUID;

public record UserMeResponse(
    UUID userId,
    UUID tenantId,
    String email,
    String firstName,
    String lastName,
    String phone,
    boolean member
) {
}
