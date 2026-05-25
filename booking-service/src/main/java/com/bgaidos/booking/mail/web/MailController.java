package com.bgaidos.booking.mail.web;

import com.bgaidos.booking.api.mail.PaymentReminderBroadcastResponse;
import com.bgaidos.booking.mail.PaymentReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final PaymentReminderService service;

    @PostMapping("/payment-reminder/broadcast")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PaymentReminderBroadcastResponse broadcastPaymentReminder() {
        return service.broadcast();
    }
}
