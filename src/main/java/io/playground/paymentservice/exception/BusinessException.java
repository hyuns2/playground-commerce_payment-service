package io.playground.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final BusinessErrorCode errorCode;

    @Override
    public String getMessage() {
        return getErrorCode().name() + ": " + getErrorCode().getMessage();
    }
}
