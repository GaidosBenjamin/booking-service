package com.bgaidos.booking.sms;

import com.bgaidos.booking.auth.service.event.BookingConfirmedEvent;
import com.bgaidos.booking.util.LocaleResolver;
import com.bgaidos.booking.util.PhoneNumbers;
import com.bgaidos.booking.util.SmsTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingSmsListener {

    private final SmsSender smsSender;
    private final SmsTemplates smsTemplates;

    @Value("${app.sms.default-country-code}")
    private String defaultCountryCode;

    @Value("${app.mail.brand-name}")
    private String brandName;

    @Async("authMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        if (event.phone() == null || event.phone().isBlank()) return;
        var e164Opt = PhoneNumbers.toE164(event.phone(), defaultCountryCode);
        if (e164Opt.isEmpty()) {
            log.warn("skipping confirmation SMS: malformed phone for booking {}", event.bookingId());
            return;
        }
        var phone = e164Opt.get();
        try {
            var locale = LocaleResolver.resolve(event.language());
            var content = smsTemplates.bookingConfirmation(locale, event.bookingId(), event.total(), event.currency(), event.camperNames(), brandName);
            smsSender.send(phone, content);
        } catch (RuntimeException ex) {
            log.warn("failed to send booking confirmation SMS to {}", phone, ex);
        }
    }
}
