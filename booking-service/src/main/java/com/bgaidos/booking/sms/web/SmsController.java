package com.bgaidos.booking.sms.web;

import com.bgaidos.booking.api.sms.BroadcastSmsRequest;
import com.bgaidos.booking.api.sms.BroadcastSmsResponse;
import com.bgaidos.booking.sms.SmsBroadcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsBroadcastService service;

    @PostMapping("/broadcast")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public BroadcastSmsResponse broadcast(@Valid @RequestBody BroadcastSmsRequest req) {
        return service.broadcast(req.textEn(), req.textRo());
    }

    @PostMapping("/payment-reminder/broadcast")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public BroadcastSmsResponse broadcastPaymentReminder() {
        return service.broadcastPaymentReminder();
    }
}
