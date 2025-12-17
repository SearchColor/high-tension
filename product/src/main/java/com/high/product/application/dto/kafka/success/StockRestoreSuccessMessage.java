package com.high.product.application.dto.kafka.success;

import java.util.UUID;

public record StockRestoreSuccessMessage(
	UUID sagaId,
	UUID orderId,
	UUID userId
) {
}
