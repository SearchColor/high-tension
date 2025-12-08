package com.high.product.infrastructure.kafka.messaging.failure;

import java.util.UUID;

public record StockReduceFailureMessage(UUID orderId,
										UUID sagaId,
										UUID couponId,
										UUID ordererId,
										String reason,
										java.util.List<StockReduceFailureItem> itemList) {}
