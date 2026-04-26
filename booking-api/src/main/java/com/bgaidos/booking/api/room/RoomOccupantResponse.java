package com.bgaidos.booking.api.room;

import java.util.UUID;

public record RoomOccupantResponse(
    UUID id,
    String firstName,
    String lastName
) {
}
