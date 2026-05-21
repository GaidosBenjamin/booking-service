package com.bgaidos.booking.payments;

import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class StripeRefundService {

    public void refund(String paymentIntentId, BigDecimal amount, String currency) {
        try {
            var params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(toLongCents(amount))
                .build();
            Refund.create(params);
            log.info("stripe partial refund issued paymentIntentId={} amount={} {}", paymentIntentId, amount, currency);
        } catch (StripeException ex) {
            log.error("stripe refund failed paymentIntentId={}: {}", paymentIntentId, ex.getMessage());
            throw new RuntimeException("Stripe refund failed: " + ex.getMessage(), ex);
        }
    }

    private static long toLongCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
    }
}
