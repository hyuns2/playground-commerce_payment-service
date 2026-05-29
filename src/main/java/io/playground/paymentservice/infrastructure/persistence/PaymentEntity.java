package io.playground.paymentservice.infrastructure.persistence;

import io.playground.paymentservice.domain.Payment;
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
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String paymentKey;

    @Column(nullable = false)
    private String orderExternalId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean isPartialCancelable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Payment.PaymentStatus status;

    public static PaymentEntity fromDomain(Payment payment) {
        return PaymentEntity.builder()
                .paymentKey(payment.getPaymentKey())
                .orderExternalId(payment.getOrderExternalId())
                .amount(payment.getAmount())
                .isPartialCancelable(payment.isPartialCancelable())
                .status(payment.getStatus())
                .build();
    }

    public Payment toDomain() {
        return Payment.of(
                this.id,
                this.paymentKey,
                this.orderExternalId,
                this.amount,
                this.isPartialCancelable,
                this.status
        );
    }
}
