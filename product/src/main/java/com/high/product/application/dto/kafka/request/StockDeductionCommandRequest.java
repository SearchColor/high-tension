package com.high.product.application.dto.kafka.request;

import java.util.UUID;

public record StockDeductionCommandRequest(
	UUID sagaId,
	UUID orderId,
	UUID userId
) {}