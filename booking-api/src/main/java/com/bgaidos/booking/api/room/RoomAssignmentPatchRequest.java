package com.bgaidos.booking.api.room;

import java.util.UUID;

public record RoomAssignmentPatchRequest(
    UUID roomId
) {
}
