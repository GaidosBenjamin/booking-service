package com.bgaidos.booking.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class BrevoSmsSender implements SmsSender {

    private final RestClient restClient;
    private final String sender;

    @Override
    public void send(String e164Phone, String content) {
        try {
            restClient.post()
                .uri("/v3/transactionalSMS/sms")
                .body(Map.of(
                    "sender", sender,
                    "recipient", e164Phone,
                    "content", content,
                    "type", "transactional",
                    "unicode", true
                ))
                .retrieve()
                .toBodilessEntity();
            log.info("SMS sent to {}", e164Phone);
        } catch (RestClientException e) {
            log.warn("failed to send SMS to {}: {}", e164Phone, e.getMessage());
        }
    }
}
