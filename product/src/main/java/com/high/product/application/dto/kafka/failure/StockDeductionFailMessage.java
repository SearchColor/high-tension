package com.high.product.application.dto.kafka.failure;

import java.util.UUID;

public record StockDeductionFailMessage(
	UUID sagaId,
	UUID orderId,
	String reason,
	UUID userId
) {
}
