package com.bgaidos.booking.payments;

import com.bgaidos.booking.api.booking.BookingItemResponse;
import com.bgaidos.booking.entity.BookingItem;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BookingMapper {

    public BookingItemResponse toItemResponse(BookingItem item, Instant holdExpiresAt) {
        return new BookingItemResponse(
            item.getCamper().getId(),
            item.getTier().getId(),
            item.getRoom().getId(),
            item.getPrice(),
            holdExpiresAt);
    }
}
