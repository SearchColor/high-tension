package com.high.order.infrastructure.kafka.dto.response;

import java.util.UUID;

public record OrderItemRequest(
    UUID productId,
    Integer quantity
) {}
