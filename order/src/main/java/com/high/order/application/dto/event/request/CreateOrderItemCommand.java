package com.high.order.application.dto.event.request;

import java.util.UUID;

public record CreateOrderItemCommand(
    UUID productId,
    Integer quantity
) {

}
