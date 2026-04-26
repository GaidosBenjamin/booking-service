package com.bgaidos.booking.api.conduct;

import java.time.Instant;
import java.util.UUID;

public record CodeOfConductAgreementResponse(
    UUID id,
    UUID codeOfConductId,
    UUID userId,
    Instant agreedOn
) {
}
