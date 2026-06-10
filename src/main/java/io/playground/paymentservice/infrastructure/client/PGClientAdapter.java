package io.playground.paymentservice.infrastructure.client;

import io.playground.paymentservice.application.dto.PGClientDto;
import io.playground.paymentservice.application.port.client.PGClientPort;
import io.playground.paymentservice.domain.Payment;
import io.playground.paymentservice.exception.BusinessDetailException;
import io.playground.paymentservice.exception.BusinessErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PGClientAdapter implements PGClientPort {
    private final WebClient webClient;
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    @Override
    public PGClientDto.ApproveResponse approvePayment(String idempotencyKey,
                                                      PGClientDto.ApproveRequest approveRequest) throws InterruptedException {
        return approveWithDummy(approveRequest);
    }

    private PGClientDto.ApproveResponse approveWithDummy(PGClientDto.ApproveRequest approveRequest) throws InterruptedException {
        Thread.sleep(300L);

        return PGClientDto.ApproveResponse.builder()
                .orderId(approveRequest.orderId())
                .paymentKey(approveRequest.paymentKey())
                .amount(approveRequest.amount())
                .isPartialCancelable(true)
                .status(Payment.PaymentStatus.SUCCESS)
                .lastTransactionKey("lastTransactionKey")
                .cancelResponses(null)
                .build();
    }

    private PGClientDto.ApproveResponse approveWithToss(String idempotencyKey,
                                                        PGClientDto.ApproveRequest approveRequest) {
        return webClient.post()
                .uri("/confirm")
                .header(IDEMPOTENCY_KEY, idempotencyKey)
                .bodyValue(approveRequest)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(PGClientDto.ApproveError.class)
                                .map(error -> new BusinessDetailException(
                                        BusinessErrorCode.PG_API_ERROR,
                                        error.message()
                                ))
                )
                .bodyToMono(PGClientDto.ApproveResponse.class)
                .block();
    }

    @Override
    public PGClientDto.ApproveResponse cancelPayment(boolean isPartially,
                                                     PGClientDto.CancelRequest cancelRequest) throws InterruptedException {
        return cancelWithDummy(cancelRequest);
    }

    private PGClientDto.ApproveResponse cancelWithDummy(PGClientDto.CancelRequest cancelRequest) throws InterruptedException {
        Thread.sleep(300L);

        return PGClientDto.ApproveResponse.builder()
                .orderId(null)
                .paymentKey(cancelRequest.paymentKey())
                .amount(cancelRequest.cancelAmount())
                .isPartialCancelable(true)
                .status(Payment.PaymentStatus.CANCELED)
                .lastTransactionKey("lastTransactionKey")
                .cancelResponses(List.of(
                        PGClientDto.CancelResponse.builder()
                                .transactionKey("lastTransactionKey")
                                .cancelAmount(cancelRequest.cancelAmount())
                                .cancelReason(cancelRequest.cancelReason())
                                .cancelStatus(Payment.PaymentStatus.SUCCESS)
                                .build()
                        )
                )
                .build();
    }

    private PGClientDto.ApproveResponse cancelWithToss(boolean isPartially,
                                                       PGClientDto.CancelRequest cancelRequest) {
        return webClient.post()
                .uri("/" + cancelRequest.paymentKey() + "/cancel")
                .header(IDEMPOTENCY_KEY, cancelRequest.idempotencyKey())
                .bodyValue(
                        isPartially ?
                                Map.of(
                                        "cancelAmount", cancelRequest.cancelAmount(),
                                        "cancelReason", cancelRequest.cancelReason()
                                ) :
                                Map.of(
                                        "cancelReason", cancelRequest.cancelReason()
                                )
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(PGClientDto.ApproveError.class)
                                .map(error -> new BusinessDetailException(
                                        BusinessErrorCode.PG_API_ERROR,
                                        error.message()
                                ))
                )
                .bodyToMono(PGClientDto.ApproveResponse.class)
                .block();
    }
}
