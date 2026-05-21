package com.bgaidos.booking.sms;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingSmsSender implements SmsSender {

    @Override
    public void send(String e164Phone, String content) {
        log.info("[DEV ONLY] SMS to {}: {}", e164Phone, content);
    }
}
