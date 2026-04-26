package com.bgaidos.booking.api.conduct;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CodeOfConductRequest(
    @NotNull Map<String, Object> content,
    Boolean active
) {
}
