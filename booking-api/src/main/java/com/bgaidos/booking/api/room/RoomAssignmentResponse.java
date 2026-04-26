package com.bgaidos.booking.api.room;

import java.time.Instant;
import java.util.UUID;

public record RoomAssignmentResponse(
    UUID id,
    UUID roomId,
    UUID camperId,
    UUID leaderId,
    Instant assignedOn,
    Instant createdOn
) {
}
