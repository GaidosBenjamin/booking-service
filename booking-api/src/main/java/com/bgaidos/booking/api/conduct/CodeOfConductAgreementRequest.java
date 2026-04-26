package com.bgaidos.booking.api.conduct;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CodeOfConductAgreementRequest(
    @NotNull UUID codeOfConductId
) {
}
