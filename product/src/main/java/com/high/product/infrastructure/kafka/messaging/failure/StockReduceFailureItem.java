package com.high.product.infrastructure.kafka.messaging.failure;

import java.util.UUID;

public record StockReduceFailureItem(UUID productId, Integer quantity) {}
