package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.UUID;

public record StockDeductionSuccessMessage(
    UUID orderId,
    UUID sagaId
    ) {
}
