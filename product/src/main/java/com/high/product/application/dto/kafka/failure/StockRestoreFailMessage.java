package com.high.product.application.dto.kafka.failure;

import java.util.UUID;

public record StockRestoreFailMessage(
	UUID sagaId,
	UUID orderId,
	String reason,
	UUID userId
) {
}
