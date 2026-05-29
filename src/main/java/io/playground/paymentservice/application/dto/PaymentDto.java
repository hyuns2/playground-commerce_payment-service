package io.playground.paymentservice.application.dto;

import java.math.BigDecimal;

public class PaymentDto {
    public record ApprovePaymentRequest(
            String idempotencyKey,
            String orderExternalId,
            String paymentKey,
            BigDecimal amount
    ) {
    }

    public record CancelPaymentRequest(
            String idempotencyKey,
            String paymentKey,
            String reason
    ) {
    }

    public record CancelPartiallyRequest(
            String idempotencyKey,
            String paymentKey,
            BigDecimal amount,
            String reason
    ) {
    }
}
