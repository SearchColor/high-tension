package com.high.order.application.dto.internal.kafka.response;

import com.high.order.application.dto.request.OrderItemRequest;
import java.util.UUID;

public record OrderItemFailResponse(
    UUID productId,
    Integer quantity
) {

    public static OrderItemFailResponse from(OrderItemRequest orderItem) {
        return new OrderItemFailResponse(
            orderItem.productId(),
            orderItem.quantity()
        );
    }
}
