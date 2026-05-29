package io.playground.paymentservice.application.dto;

import io.playground.paymentservice.domain.Payment;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class PGClientDto {
    @Builder
    public record ApproveRequest(
            String orderId,
            String paymentKey,
            BigDecimal amount
    ) {
    }

    @Builder
    public record CancelRequest(
            String idempotencyKey,
            String paymentKey,
            BigDecimal cancelAmount,
            String cancelReason
    ) {
    }

    @Builder
    public record ApproveResponse(
            String orderId,
            String paymentKey,
            BigDecimal amount,
            boolean isPartialCancelable,
            Payment.PaymentStatus status,
            String lastTransactionKey,
            List<CancelResponse> cancelResponses
    ) {
    }

    @Builder
    public record CancelResponse(
            String transactionKey,
            BigDecimal cancelAmount,
            String cancelReason,
            Payment.PaymentStatus cancelStatus
    ) {
    }

    public record ApproveError(
            String code,
            String message
    ) {
    }
}
