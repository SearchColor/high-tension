package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.UUID;

public record PaymentCreateSuccessMessage(
    UUID sagaId,
    UUID orderId
) {

}
