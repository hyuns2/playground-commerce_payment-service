package io.playground.paymentservice.application.port.client;

import io.playground.paymentservice.application.dto.PGClientDto;

public interface PGClientPort {
    PGClientDto.ApproveResponse approvePayment(String idempotencyKey,
                                               PGClientDto.ApproveRequest approveRequest) throws InterruptedException;

    PGClientDto.ApproveResponse cancelPayment(boolean isPartially,
                                              PGClientDto.CancelRequest cancelRequest) throws InterruptedException;
}
