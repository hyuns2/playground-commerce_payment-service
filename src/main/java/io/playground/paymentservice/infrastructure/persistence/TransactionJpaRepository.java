package io.playground.paymentservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
    boolean existsByIdempotencyKeyAndPayment_PaymentKey(String idempotencyKey,
                                                        String paymentKey);
    List<TransactionEntity> findAllByPaymentId(Long paymentId);
}
