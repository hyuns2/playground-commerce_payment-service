package io.playground.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessErrorCode {
    // 400
    PAYMENT_APPROVE_FAILED("PAYMENT-400:001", "결제에 실패했습니다. 다시 시도해주세요.", HttpStatus.BAD_REQUEST),
    PAYMENT_CANCELLATION_FAILED("PAYMENT-400:002", "결제 취소에 실패했습니다. 다시 시도해주세요.", HttpStatus.BAD_REQUEST),
    PAYMENT_PARTIAL_CANCELLATION_FAILED("PAYMENT-400:003", "부분 취소에 실패했습니다. 다시 시도해주세요.", HttpStatus.BAD_REQUEST),

    // 404

    // 500
    JSON_PROCESSING_FAILED("Payment-500:001", "JSON 직렬화 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PG_API_ERROR("Payment-500:002", "PG API 호출 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
