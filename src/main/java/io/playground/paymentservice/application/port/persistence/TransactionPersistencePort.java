package io.playground.paymentservice.application.port.persistence;

import io.playground.paymentservice.domain.Transaction;

import java.util.List;

public interface TransactionPersistencePort {
    boolean existsByPaymentKeyAndIdempotencyKey(String paymentKey,
                                                String idempotencyKey);
    List<Transaction> findAllByPaymentId(Long paymentId);
    Transaction save(Transaction transaction);
}
