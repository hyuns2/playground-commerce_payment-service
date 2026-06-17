package io.playground.paymentservice.application.usecase;

import io.playground.paymentservice.application.dto.PGClientDto;
import io.playground.paymentservice.application.port.persistence.PaymentPersistencePort;
import io.playground.paymentservice.application.port.persistence.TransactionPersistencePort;
import io.playground.paymentservice.domain.Payment;
import io.playground.paymentservice.domain.Transaction;
import io.playground.paymentservice.exception.BusinessDetailException;
import io.playground.paymentservice.exception.BusinessErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTxService {
    private final PaymentPersistencePort paymentPersistence;
    private final TransactionPersistencePort transactionPersistence;

    @Transactional
    public void approvePayment(PGClientDto.ApproveResponse response,
                               String idempotencyKey) {
        // 결제 정보 저장
        Payment payment = paymentPersistence.save(
                Payment.of(
                        null,
                        response.paymentKey(),
                        response.orderId(),
                        response.amount(),
                        response.isPartialCancelable(),
                        response.status()
                )
        );

        // 거래내역 저장
        transactionPersistence.save(
                Transaction.of(
                        null,
                        payment.getId(),
                        Transaction.TransactionType.APPROVE,
                        response.amount(),
                        idempotencyKey,
                        response.lastTransactionKey()
                )
        );
    }

    @Transactional
    public void cancelPayment(Long paymentId,
                              PGClientDto.ApproveResponse response,
                              String idempotencyKey) {
        // 취소 거래내역 저장
        transactionPersistence.save(
                Transaction.of(
                        null,
                        paymentId,
                        Transaction.TransactionType.CANCEL,
                        response.cancelResponses()
                                .get(0)
                                .cancelAmount(),
                        idempotencyKey,
                        response.lastTransactionKey()
                )
        );

        // 결제 상태 전체취소로 업데이트
        if (!paymentPersistence.updateStatusById(
                paymentId,
                Payment.PaymentStatus.CANCELED,
                List.of(Payment.PaymentStatus.SUCCESS)
        ))
            throw new BusinessDetailException(
                    BusinessErrorCode.PAYMENT_CANCELLATION_FAILED,
                    "PAYMENT_STATUS_UPDATE_FAILED"
            );
    }

    @Transactional
    public void cancelPartially(Long paymentId,
                                PGClientDto.ApproveResponse response,
                                String idempotencyKey) {
        // 부분취소 거래 저장
        transactionPersistence.save(
                Transaction.of(
                        null,
                        paymentId,
                        Transaction.TransactionType.PARTIAL_CANCEL,
                        response.cancelResponses().stream()
                                .map(PGClientDto.CancelResponse::cancelAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        idempotencyKey,
                        response.lastTransactionKey()
                )
        );

        // 결제 상태 부분취소로 업데이트
        if (!paymentPersistence.updateStatusById(
                paymentId,
                Payment.PaymentStatus.PARTIAL_CANCELED,
                List.of(
                        Payment.PaymentStatus.SUCCESS,
                        Payment.PaymentStatus.PARTIAL_CANCELED
                )
        ))
            throw new BusinessDetailException(
                    BusinessErrorCode.PAYMENT_CANCELLATION_FAILED,
                    "PAYMENT_STATUS_UPDATE_FAILED"
            );
    }
}
