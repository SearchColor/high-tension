package com.high.order.application.dto.internal.kafka.response;

import com.high.order.domain.entity.OrderItem;
import java.util.UUID;

public record OrderItemSuccessResponse(
    UUID productId,
    Integer quantity
) {

    public static  OrderItemSuccessResponse from(OrderItem orderItem) {
        return new OrderItemSuccessResponse(
            orderItem.getProductId(),
            orderItem.getQuantity()
        );
    }
}
