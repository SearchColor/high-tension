package com.high.product.infrastructure.kafka.messaging.success;

import java.util.UUID;

public record StockReduceSuccessItem(UUID productId, Integer quantity) {}
