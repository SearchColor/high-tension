package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.UUID;

public record OrderCreateSuccessMessage(
    UUID sagaId,
    UUID orderId
) {

}
