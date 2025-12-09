package com.high.product.infrastructure.kafka.messaging.failure;

import java.util.UUID;

public record StockDeductionFailMessage(
	UUID sagaId,
	UUID orderId,
	String reason,
	UUID userId
) {
}
