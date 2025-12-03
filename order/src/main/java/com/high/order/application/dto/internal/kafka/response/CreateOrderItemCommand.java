package com.high.order.application.dto.internal.kafka.response;

import java.util.UUID;

public record CreateOrderItemCommand(
    UUID productId,
    Integer quantity
) {

}
