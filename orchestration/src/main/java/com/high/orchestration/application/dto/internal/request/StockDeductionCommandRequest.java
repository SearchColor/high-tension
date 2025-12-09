package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record StockDeductionCommandRequest(
    UUID sagaId,
    UUID orderId,
    UUID userId
) {

}
