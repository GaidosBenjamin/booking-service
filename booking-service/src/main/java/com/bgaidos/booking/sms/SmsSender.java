package com.bgaidos.booking.sms;

public interface SmsSender {
    void send(String e164Phone, String content);
}
