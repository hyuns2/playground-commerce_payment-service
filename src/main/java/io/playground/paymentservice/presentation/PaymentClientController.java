package io.playground.paymentservice.presentation;

import io.playground.paymentservice.application.dto.PaymentDto;
import io.playground.paymentservice.application.usecase.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment/client")
@RequiredArgsConstructor
public class PaymentClientController {
    private final PaymentService paymentService;

    @PostMapping("/approve")
    public ResponseEntity<Void> approvePayment(@RequestBody PaymentDto.ApprovePaymentRequest request) {
        paymentService.approvePayment(
                request.idempotencyKey(),
                request.orderExternalId(),
                request.paymentKey(),
                request.amount()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelPayment(@RequestBody PaymentDto.CancelPaymentRequest request) {
        paymentService.cancelPayment(
                request.idempotencyKey(),
                request.paymentKey(),
                request.reason()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/partial-cancel")
    public ResponseEntity<Void> cancelPartially(@RequestBody PaymentDto.CancelPartiallyRequest request) {
        paymentService.cancelPartially(
                request.idempotencyKey(),
                request.paymentKey(),
                request.amount(),
                request.reason()
        );

        return ResponseEntity.ok().build();
    }
}
