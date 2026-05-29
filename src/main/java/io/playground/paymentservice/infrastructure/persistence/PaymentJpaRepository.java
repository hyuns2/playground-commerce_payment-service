package io.playground.paymentservice.infrastructure.persistence;

import io.playground.paymentservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByPaymentKey(String paymentKey);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PaymentEntity p " +
                "set p.status = :status " +
            "where p.id = :id and " +
                "p.status in :beforeStatuses")
    int updateStatusById(Long id,
                         Payment.PaymentStatus status,
                         List<Payment.PaymentStatus> beforeStatuses);
}
