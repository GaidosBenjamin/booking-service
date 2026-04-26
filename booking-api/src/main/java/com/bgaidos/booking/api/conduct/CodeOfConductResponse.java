package com.bgaidos.booking.api.conduct;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CodeOfConductResponse(
    UUID id,
    Map<String, Object> content,
    boolean active,
    Instant createdOn
) {
}
