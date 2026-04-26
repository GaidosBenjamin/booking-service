package com.bgaidos.booking.payments.web;

import com.bgaidos.booking.payments.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService webhookService;

    @PostMapping
    public ResponseEntity<Void> handle(
        @RequestBody byte[] payload,
        @RequestHeader("Stripe-Signature") String signature
    ) {
        try {
            webhookService.handle(payload, signature);
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException ex) {
            log.warn("stripe webhook: signature verification failed");
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            log.error("stripe webhook: unhandled error — raw payload for manual replay: {}",
                new String(payload, StandardCharsets.UTF_8), ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
