package com.bgaidos.booking.payments;

import com.bgaidos.booking.api.donation.DonationRequest;
import com.bgaidos.booking.api.donation.DonationResponse;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.config.StripeConfig;
import com.bgaidos.booking.entity.Donation;
import com.bgaidos.booking.entity.PaymentStatus;
import com.bgaidos.booking.repo.DonationRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final StripeConfig stripeConfig;
    private final PlatformTransactionManager txManager;

    public DonationResponse create(DonationRequest request) {
        var currency = request.currency().toLowerCase();

        // Pre-allocate ID so it can be embedded in return URLs before the write transaction
        var donation = new Donation();
        donation.setId();

        // Stripe API call outside any DB transaction
        var session = createCheckoutSession(request.amount(), currency, donation.getId());

        // Write transaction — persist donation
        var writeTx = new TransactionTemplate(txManager);
        var saved = Objects.requireNonNull(writeTx.execute(status -> {
            donation.setName(request.name());
            donation.setOrgSlug(request.orgSlug());
            donation.setAmount(request.amount());
            donation.setCurrency(request.currency().toUpperCase());
            donation.setStatus(PaymentStatus.PENDING);
            donation.setStripeSessionId(session.getId());
            donation.setExpiresAt(Instant.ofEpochSecond(session.getExpiresAt()));
            return donationRepository.save(donation);
        }));

        log.info("created donation id={} sessionId={} amount={} currency={}", saved.getId(), session.getId(), request.amount(), currency);
        return toResponse(saved, session.getUrl());
    }

    @Transactional(readOnly = true)
    public DonationResponse get(UUID id) {
        var donation = donationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("donation not found: " + id));
        return toResponse(donation, null);
    }

    public void onWebhookSucceeded(Session session) {
        var updated = donationRepository.updateStatus(session.getId(), PaymentStatus.PENDING, PaymentStatus.SUCCEEDED);
        if (updated == 0) {
            var donation = donationRepository.findByStripeSessionId(session.getId()).orElse(null);
            if (donation == null) {
                log.warn("checkout.session completed: no donation found for sessionId={}", session.getId());
            } else {
                log.debug("checkout.session idempotent: donationId={} already {}", donation.getId(), donation.getStatus());
            }
            return;
        }
        log.info("donation SUCCEEDED sessionId={}", session.getId());
    }

    public void onWebhookExpiredOrFailed(String eventType, Session session) {
        donationRepository.findByStripeSessionId(session.getId()).ifPresent(d -> {
            var newStatus = "checkout.session.expired".equals(eventType)
                ? PaymentStatus.CANCELED
                : PaymentStatus.FAILED;
            d.setStatus(newStatus);
            log.info("donation {} sessionId={}", newStatus, session.getId());
        });
    }

    private Session createCheckoutSession(BigDecimal amount, String currency, UUID donationId) {
        try {
            return Session.create(SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                    .setDescription(StripeEntityType.DONATION.description)
                    .build())
                .setSubmitType(SessionCreateParams.SubmitType.DONATE)
                .setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES).getEpochSecond())
                .setSuccessUrl(stripeConfig.getDonationSuccessUrl() + "?donationId=" + donationId)
                .setCancelUrl(stripeConfig.getDonationCancelUrl() + "?donationId=" + donationId)
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency)
                        .setUnitAmount(toLongCents(amount))
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Donation")
                            .build())
                        .build())
                    .setQuantity(1L)
                    .build())
                .putMetadata("entityType", StripeEntityType.DONATION.name())
                .putMetadata("donationId", donationId.toString())
                .build());
        } catch (StripeException ex) {
            throw new RuntimeException("Stripe Checkout Session creation failed: " + ex.getMessage(), ex);
        }
    }

    private static DonationResponse toResponse(Donation donation, String checkoutUrl) {
        return new DonationResponse(
            donation.getId(),
            donation.getAmount(),
            donation.getCurrency(),
            donation.getStatus().name(),
            checkoutUrl,
            donation.getExpiresAt());
    }

    private static long toLongCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
    }
}
