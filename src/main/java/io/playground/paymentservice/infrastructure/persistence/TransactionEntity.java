package io.playground.paymentservice.infrastructure.persistence;

import io.playground.paymentservice.domain.Transaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "transactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idempotencyKey_paymentId",
                columnNames = {"idempotencyKey", "paymentId"}
        )
)
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id",nullable = false)
    private PaymentEntity payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String transactionKey;

    public static TransactionEntity fromDomain(Transaction transaction,
                                               PaymentEntity paymentEntity) {
        return TransactionEntity.builder()
                .payment(paymentEntity)
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .idempotencyKey(transaction.getIdempotencyKey())
                .transactionKey(transaction.getTransactionKey())
                .build();
    }

    public Transaction toDomain() {
        return Transaction.of(
                this.id,
                this.payment.getId(),
                this.type,
                this.amount,
                this.idempotencyKey,
                this.transactionKey
        );
    }
}
