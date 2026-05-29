package io.playground.paymentservice.infrastructure.persistence;

import io.playground.paymentservice.application.port.persistence.PaymentPersistencePort;
import io.playground.paymentservice.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentPersistencePort {
    private final PaymentJpaRepository paymentRepository;

    @Override
    public Optional<Payment> findByPaymentKey(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(
                PaymentEntity.fromDomain(payment)
        ).toDomain();
    }

    @Override
    public boolean updateStatusById(Long id,
                                    Payment.PaymentStatus status,
                                    List<Payment.PaymentStatus> beforeStatuses) {
        return paymentRepository.updateStatusById(
                id, status, beforeStatuses
        ) > 0;
    }
}
