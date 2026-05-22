package com.bgaidos.booking.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SmsTemplates {

    private final MessageSource messageSource;

    public String bookingConfirmation(Locale locale, UUID bookingId, BigDecimal total, String currency, List<String> camperNames, String brandName) {
        var names = String.join(", ", camperNames);
        var shortId = bookingId.toString().substring(0, 8).toUpperCase();
        return messageSource.getMessage("sms.booking.confirmation",
            new Object[]{names, total.stripTrailingZeros().toPlainString(), currency, shortId, brandName},
            locale);
    }
}
