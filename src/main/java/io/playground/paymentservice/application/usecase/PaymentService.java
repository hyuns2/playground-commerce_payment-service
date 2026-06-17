package io.playground.paymentservice.application.usecase;

import io.playground.paymentservice.application.dto.PGClientDto;
import io.playground.paymentservice.application.port.client.PGClientPort;
import io.playground.paymentservice.application.port.persistence.PaymentPersistencePort;
import io.playground.paymentservice.application.port.persistence.TransactionPersistencePort;
import io.playground.paymentservice.domain.Payment;
import io.playground.paymentservice.domain.Transaction;
import io.playground.paymentservice.exception.BusinessDetailException;
import io.playground.paymentservice.exception.BusinessErrorCode;
import io.playground.paymentservice.exception.BusinessException;
import io.playground.paymentservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// ToDo: PG사 에러 세분화 필요
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentPersistencePort paymentPersistence;
    private final TransactionPersistencePort transactionPersistence;
    private final PGClientPort pgClient;
    private final PaymentTxService paymentTxService;
    private final JsonUtil jsonUtil;

    /**
     * PG사에 결제 승인 요청 -> 승인 성공 시 결제 정보 저장
     *
     * @param idempotencyKey 멱등성 보장키
     * @param orderExternalId 주문번호
     * @param paymentKey PG사에서 발급한 결제 고유키
     * @param amount 결제 승인 금액
     */
    public void approvePayment(String idempotencyKey,
                               String orderExternalId,
                               String paymentKey,
                               BigDecimal amount) {
        // 중복 결제 방지 위해 기존 결제 정보 조회
        if (transactionPersistence
                .existsByPaymentKeyAndIdempotencyKey(
                        paymentKey, idempotencyKey))
            return;

        // 결제 승인 요청
        PGClientDto.ApproveResponse response;
        try {
            response = pgClient.approvePayment(
                    idempotencyKey,
                    PGClientDto.ApproveRequest.builder()
                            .orderId(orderExternalId)
                            .paymentKey(paymentKey)
                            .amount(amount)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessDetailException(
                    BusinessErrorCode.PG_API_ERROR,
                    e.getMessage()
            );
        }

        // 정상 승인 확인
        if (response.status() != Payment.PaymentStatus.SUCCESS)
            throw new BusinessException(
                    BusinessErrorCode.PAYMENT_APPROVE_FAILED
            );

        // 결제정보 및 거래내역 저장
        paymentTxService.approvePayment(
                response,
                idempotencyKey
        );
    }

    /**
     * PG사에 전체취소 요청 -> 취소 성공 시 정보 업데이트
     *
     * @param idempotencyKey 멱등성 보장키
     * @param paymentKey PG사에서 발급한 결제 고유키
     * @param reason 취소 사유
     */
    public void cancelPayment(String idempotencyKey,
                              String paymentKey,
                              String reason) {
        // 결제 정보 조회
        Payment payment = paymentPersistence
                .findByPaymentKey(paymentKey)
                .orElseThrow(() -> new BusinessDetailException(
                        BusinessErrorCode.PAYMENT_CANCELLATION_FAILED,
                        "PAYMENT_NOT_FOUND"
                ));

        if (transactionPersistence
                .existsByPaymentKeyAndIdempotencyKey(
                        paymentKey, idempotencyKey))
            return;

        // 결제 승인 상태가 아니라면 전체 환불 불가
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS)
            throw new BusinessDetailException(
                    BusinessErrorCode.PAYMENT_CANCELLATION_FAILED,
                    jsonUtil.toJson(
                            Map.of(
                                    "paymentStatus", payment.getStatus()
                            )
                    )
            );

        // 결제 취소 요청
        PGClientDto.ApproveResponse response;
        try {
            response = pgClient.cancelPayment(
                    false,
                    PGClientDto.CancelRequest.builder()
                            .idempotencyKey(idempotencyKey)
                            .paymentKey(payment.getPaymentKey())
                            .cancelAmount(payment.getAmount())
                            .cancelReason(reason)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessDetailException(
                    BusinessErrorCode.PAYMENT_CANCELLATION_FAILED,
                    e.getMessage()
            );
        }

        // 정상 취소 확인
        if (response.status() != Payment.PaymentStatus.CANCELED ||
                response.cancelResponses().size() != 1)
            throw new BusinessException(
                    BusinessErrorCode.PAYMENT_CANCELLATION_FAILED
            );

        // 결제정보 업데이트 및 거래내역 저장
        paymentTxService.cancelPayment(
                payment.getId(),
                response,
                idempotencyKey
        );
    }

    /**
     * PG사에 부분취소 요청 -> 취소 성공 시 정보 업데이트
     *
     * @param idempotencyKey 멱등성 보장키
     * @param paymentKey PG사에서 발급한 결제 고유키
     * @param amount 부분취소 금액
     * @param reason 부분취소 사유
     */
    public void cancelPartially(String idempotencyKey,
                                String paymentKey,
                                BigDecimal amount,
                                String reason) {
        // 결제 정보 조회
        Payment payment = paymentPersistence
                .findByPaymentKey(paymentKey)
                .orElseThrow(() -> new BusinessDetailException(
                        BusinessErrorCode.PAYMENT_PARTIAL_CANCELLATION_FAILED,
                        "PAYMENT_NOT_FOUND"
                ));

        List<Transaction> transactions = transactionPersistence
                .findAllByPaymentId(payment.getId());
        if (transactions.stream()
                .anyMatch(t -> t.getIdempotencyKey().equals(idempotencyKey) &&
                        t.getType() == Transaction.TransactionType.PARTIAL_CANCEL))
            return;

        // 부분취소 불가 또는 승인/부분취소 상태가 아니라면, 부분취소 불가
        if (
                !payment.isPartialCancelable() ||
                !(payment.getStatus() == Payment.PaymentStatus.SUCCESS ||
                        payment.getStatus() == Payment.PaymentStatus.PARTIAL_CANCELED)
        )
            throw new BusinessDetailException(
                    BusinessErrorCode.PAYMENT_PARTIAL_CANCELLATION_FAILED,
                    jsonUtil.toJson(
                            Map.of(
                                    "isPartialCancelable", payment.isPartialCancelable(),
                                    "status", payment.getStatus()
                            )
                    )
            );

        // 부분취소 가능한 금액을 초과한다면 부분취소 불가
        BigDecimal balanceAmount = transactions.stream()
                .map(t -> t.getType() == Transaction.TransactionType.APPROVE ?
                        t.getAmount() :
                        t.getAmount().negate()
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (balanceAmount.compareTo(amount) < 0)
            throw new BusinessDetailException(
                    BusinessErrorCode.PAYMENT_PARTIAL_CANCELLATION_FAILED,
                    jsonUtil.toJson(
                            Map.of("balanceAmount", balanceAmount)
                    )
            );

        // 부분취소 요청
        PGClientDto.ApproveResponse response;
        try {
            response = pgClient.cancelPayment(
                    true,
                    PGClientDto.CancelRequest.builder()
                            .idempotencyKey(idempotencyKey)
                            .paymentKey(payment.getPaymentKey())
                            .cancelAmount(amount)
                            .cancelReason(reason)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessDetailException(
                    BusinessErrorCode.PAYMENT_PARTIAL_CANCELLATION_FAILED,
                    e.getMessage()
            );
        }

        // 정상 부분취소 확인
        if (response.cancelResponses().stream()
                .filter(
                        cancelResponse ->
                                cancelResponse.transactionKey().equals(
                                        response.lastTransactionKey()
                        )
                ).noneMatch(
                        cancelResponse ->
                                cancelResponse.cancelStatus() ==
                                        Payment.PaymentStatus.SUCCESS
                )
        )
            throw new BusinessException(
                    BusinessErrorCode.PAYMENT_PARTIAL_CANCELLATION_FAILED
            );

        // 결제정보 업데이트 및 거래내역 저장
        paymentTxService.cancelPartially(
                payment.getId(),
                response,
                idempotencyKey
        );
    }
}
