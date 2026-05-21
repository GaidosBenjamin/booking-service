package com.bgaidos.booking.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SmsTemplates {

    private final MessageSource messageSource;

    public String bookingConfirmation(Locale locale, String brandName) {
        return messageSource.getMessage("sms.booking.confirmation", new Object[]{brandName}, locale);
    }
}
