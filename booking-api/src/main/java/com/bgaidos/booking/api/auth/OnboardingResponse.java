package com.bgaidos.booking.api.auth;

import java.util.UUID;

public record OnboardingResponse(UUID organizationId, String slug) {
}
