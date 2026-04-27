package com.bgaidos.booking.payments;

import com.bgaidos.booking.api.booking.BookingItemResponse;
import com.bgaidos.booking.entity.BookingItem;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BookingMapper {

    public BookingItemResponse toItemResponse(BookingItem item, Instant holdExpiresAt) {
        var tier = item.getTier();
        var camper = item.getCamper();
        return new BookingItemResponse(
            item.getRoom().getId(),
            item.getPrice(),
            holdExpiresAt,
            new BookingItemResponse.TierSummary(
                tier.getId(),
                tier.getName(),
                tier.getCurrency(),
                tier.getBasePrice(),
                tier.getDiscountPrice()),
            new BookingItemResponse.CamperSummary(
                camper.getId(),
                camper.getFirstName(),
                camper.getLastName(),
                camper.getDateOfBirth(),
                camper.getGrade(),
                camper.getGender()
            )
        );
    }
}
