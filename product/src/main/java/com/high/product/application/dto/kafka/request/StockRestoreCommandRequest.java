package com.high.product.application.dto.kafka.request;

import java.util.UUID;

public record StockRestoreCommandRequest(
	UUID sagaId,
	UUID orderId,
	UUID userId
) {
}