package com.high.product.infrastructure.kafka.messaging.success;

import java.util.UUID;

public record StockDeductionSuccessMessage(
	UUID orderId,
	UUID sagaId,
	UUID userId
) {
}