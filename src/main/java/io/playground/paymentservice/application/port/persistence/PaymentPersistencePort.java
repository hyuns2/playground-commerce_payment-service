package io.playground.paymentservice.application.port.persistence;

import io.playground.paymentservice.domain.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentPersistencePort {
    Optional<Payment> findByPaymentKey(String orderExternalId);

    Payment save(Payment payment);

    boolean updateStatusById(Long id,
                             Payment.PaymentStatus status,
                             List<Payment.PaymentStatus> beforeStatuses);
}
