package io.playground.paymentservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BusinessExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<BusinessErrorDto> handleException(BusinessException e) {
        BusinessErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(
                        new BusinessErrorDto(
                                errorCode, null
                        )
                );
    }

    @ExceptionHandler(BusinessDetailException.class)
    protected ResponseEntity<BusinessErrorDto> handleException(BusinessDetailException e) {
        BusinessErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(
                        new BusinessErrorDto(
                                errorCode, e.getDetail()
                        )
                );
    }
}
