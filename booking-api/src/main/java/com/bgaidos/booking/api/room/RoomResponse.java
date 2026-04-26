package com.bgaidos.booking.api.room;

import java.util.List;
import java.util.UUID;

public record RoomResponse(
    UUID id,
    String name,
    int capacity,
    String imageUrl,
    boolean leaderRoom,
    List<RoomOccupantResponse> assignments,
    List<RoomOccupantResponse> holds
) {
}
