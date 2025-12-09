package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.UUID;

public record StockDeductionFailMessage(
    UUID sagaId,
    UUID orderId,
    String reason,
    UUID userId

) {

}
