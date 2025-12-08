package com.high.product.infrastructure.kafka.messaging.request;

import java.util.UUID;

public record StockReduceItem(UUID productId, Integer quantity) {}
