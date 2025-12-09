package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.UUID;

public record PaymentCreateFailMessage(
    UUID sagaId,
    UUID orderId,
    String reason
) {

}
