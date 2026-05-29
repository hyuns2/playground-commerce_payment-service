package io.playground.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessDetailException extends RuntimeException {
    private final BusinessErrorCode errorCode;
    private final String detail;

    @Override
    public String getMessage() {
        return getErrorCode().name() + ": " + getErrorCode().getMessage();
    }

    public String getDetail() {
        return detail;
    }
}
