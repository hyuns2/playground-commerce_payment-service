package io.playground.paymentservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Transaction {
    private Long id;

    private Long paymentId;

    private TransactionType type;

    private BigDecimal amount;

    private String idempotencyKey;

    private String transactionKey;

    public enum TransactionType {
        APPROVE, CANCEL, PARTIAL_CANCEL
    }

    public static Transaction of(Long id,
                                 Long paymentId,
                                 TransactionType type,
                                 BigDecimal amount,
                                 String idempotencyKey,
                                 String transactionKey) {
        return new Transaction(id, paymentId, type, amount, idempotencyKey, transactionKey);
    }
}
