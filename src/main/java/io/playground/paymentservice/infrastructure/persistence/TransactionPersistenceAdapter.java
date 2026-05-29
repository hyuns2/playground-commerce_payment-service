package io.playground.paymentservice.infrastructure.persistence;

import io.playground.paymentservice.application.port.persistence.TransactionPersistencePort;
import io.playground.paymentservice.domain.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionPersistencePort {
    private final TransactionJpaRepository transactionRepository;
    private final PaymentJpaRepository paymentRepository;

    @Override
    public boolean existsByPaymentKeyAndIdempotencyKey(String paymentKey,
                                                       String idempotencyKey) {
        return transactionRepository
                .existsByPayment_PaymentKeyAndIdempotencyKey(
                        paymentKey, idempotencyKey
                );
    }

    @Override
    public List<Transaction> findAllByPaymentId(Long paymentId) {
        return transactionRepository
                .findAllByPaymentId(paymentId).stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(
                TransactionEntity.fromDomain(
                        transaction,
                        paymentRepository.getReferenceById(
                                transaction.getPaymentId()
                        )
                )
        ).toDomain();
    }
}
