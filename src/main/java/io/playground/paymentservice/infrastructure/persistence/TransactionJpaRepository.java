package io.playground.paymentservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
    boolean existsByPayment_PaymentKeyAndIdempotencyKey(String paymentKey,
                                                        String idempotencyKey);
    List<TransactionEntity> findAllByPaymentId(Long paymentId);
}
