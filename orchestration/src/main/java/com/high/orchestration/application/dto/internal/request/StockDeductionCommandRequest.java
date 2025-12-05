package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record StockDeductionCommandRequest(
    UUID orderId,
    UUID sagaId
) {

}
