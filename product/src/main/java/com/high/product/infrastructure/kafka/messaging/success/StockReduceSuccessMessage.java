package com.high.product.infrastructure.kafka.messaging.success;

import java.util.UUID;

public record StockReduceSuccessMessage(UUID orderId, UUID sagaId, UUID couponId, UUID ordererId, java.util.List<StockReduceSuccessItem> itemList) {}