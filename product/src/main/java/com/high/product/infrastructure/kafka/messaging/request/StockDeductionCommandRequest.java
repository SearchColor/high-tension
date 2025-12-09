package com.high.product.infrastructure.kafka.messaging.request;

import java.util.UUID;

public record StockDeductionCommandRequest(
	UUID sagaId,
	UUID orderId,
	UUID userId
) {}