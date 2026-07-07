package com.bgaidos.booking.payments;

import com.bgaidos.booking.config.StripeConfig;
import com.bgaidos.booking.entity.CamperStatus;
import com.bgaidos.booking.entity.PaymentStatus;
import com.bgaidos.booking.repo.BookingItemRepository;
import com.bgaidos.booking.repo.BookingRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingConfirmationService confirmationService;
    private final DonationService donationService;

    public void handle(byte[] rawPayload, String signature) throws SignatureVerificationException {
        var event = Webhook.constructEvent(
            new String(rawPayload, StandardCharsets.UTF_8),
            signature,
            stripeConfig.getWebhookSecret());

        log.info("stripe webhook received type={}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed",
                 "checkout.session.async_payment_succeeded" -> {
                var session = extractSession(event);
                if (session == null) break;
                if (isBookingSession(session)) onSucceeded(session);
                else if (isDonationSession(session)) donationService.onWebhookSucceeded(session);
            }
            case "checkout.session.expired",
                 "checkout.session.async_payment_failed" -> {
                var session = extractSession(event);
                if (session == null) break;
                if (isBookingSession(session)) onExpiredOrFailed(event.getType(), session);
                else if (isDonationSession(session)) donationService.onWebhookExpiredOrFailed(event.getType(), session);
            }
            case "payment_intent.created",
                 "payment_intent.succeeded",
                 "payment_intent.payment_failed",
                 "payment_intent.canceled" -> log.debug("payment_intent lifecycle event type={}", event.getType());
            default -> log.warn("unhandled stripe event type={}", event.getType());
        }
    }

    private void onSucceeded(Session session) {
        var updated = bookingRepository.updateStatus(session.getId(), PaymentStatus.PENDING, PaymentStatus.SUCCEEDED);
        if (updated == 0) {
            var booking = bookingRepository.findByStripeSessionId(session.getId()).orElse(null);
            if (booking == null) {
                log.warn("checkout.session completed: no booking found for sessionId={}", session.getId());
            } else {
                log.debug("checkout.session idempotent: bookingId={} already {}", booking.getId(), booking.getStatus());
            }
            return;
        }
        var booking = bookingRepository.findByStripeSessionId(session.getId()).orElseThrow();
        booking.setStripePaymentIntentId(session.getPaymentIntent());
        var items = bookingItemRepository.findAllByBookingId(booking.getId());
        confirmationService.confirmAllItems(booking, items);
        log.info("booking SUCCEEDED id={} sessionId={} assignments={}", booking.getId(), session.getId(), items.size());
    }

    private void onExpiredOrFailed(String eventType, Session session) {
        bookingRepository.findByStripeSessionId(session.getId()).ifPresent(b -> {
            var newStatus = "checkout.session.expired".equals(eventType)
                ? PaymentStatus.CANCELED
                : PaymentStatus.FAILED;
            b.setStatus(newStatus);
            log.info("booking {} id={} sessionId={}", newStatus, b.getId(), session.getId());

            if (newStatus == PaymentStatus.FAILED) {
                var items = bookingItemRepository.findAllByBookingId(b.getId());
                items.forEach(item -> item.getCamper().setStatus(CamperStatus.PAYMENT_FAILED));
            }
        });
    }

    private static boolean isBookingSession(Session session) {
        return hasEntityType(session, StripeEntityType.BOOKING);
    }

    private static boolean isDonationSession(Session session) {
        return hasEntityType(session, StripeEntityType.DONATION);
    }

    private static boolean hasEntityType(Session session, StripeEntityType expected) {
        var entityType = session.getMetadata() != null ? session.getMetadata().get("entityType") : null;
        return expected.name().equals(entityType);
    }

    private static Session extractSession(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()
            && deserializer.getObject().get() instanceof Session s) {
            return s;
        }
        try {
            var obj = deserializer.deserializeUnsafe();
            log.debug("deserialized unsafe obj");
            if (obj instanceof Session s) return s;
        } catch (Exception e) {
            log.warn("could not deserialize Session from event type={}: {}", event.getType(), e.getMessage());
            return null;
        }
        log.warn("could not deserialize Session from event type={}", event.getType());
        return null;
    }
}
