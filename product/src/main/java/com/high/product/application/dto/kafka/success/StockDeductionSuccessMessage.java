package com.high.product.application.dto.kafka.success;

import java.util.UUID;

public record StockDeductionSuccessMessage(
	UUID orderId,
	UUID sagaId,
	UUID userId
) {
}