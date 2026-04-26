package com.bgaidos.booking.api.booking;

import java.util.List;
import java.util.UUID;

public record BookingCreateRequest(
    List<UUID> camperIds
) {
}
