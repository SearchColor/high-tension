package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record StockRestoreCommandRequest(
    UUID sagaId,
    UUID orderId,
    UUID userId
) {

}
