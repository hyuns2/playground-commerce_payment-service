package io.playground.paymentservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Payment {
    private Long id;

    private String paymentKey;

    private String orderExternalId;

    private BigDecimal amount;

    private boolean isPartialCancelable;

    private PaymentStatus status;

    public enum PaymentStatus {
        SUCCESS,
        CANCELED, PARTIAL_CANCELED
    }

    public static Payment of(Long id,
                             String paymentKey,
                             String orderExternalId,
                             BigDecimal amount,
                             boolean isPartialCancelable,
                             PaymentStatus status) {
        return new Payment(id, paymentKey, orderExternalId, amount, isPartialCancelable, status);
    }
}
