package com.bgaidos.booking.auth.service.event;

import java.time.Duration;

public record VerificationCodeIssuedEvent(String email, String code, Duration expiresIn) {
}
