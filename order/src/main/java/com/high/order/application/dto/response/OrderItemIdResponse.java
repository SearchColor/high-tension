package com.high.order.application.dto.response;

import com.high.order.domain.entity.OrderItem;
import java.util.UUID;

public record OrderItemIdResponse(
    UUID orderItemId
) {

    public static OrderItemIdResponse from(OrderItem orderItem) {
        return new OrderItemIdResponse(orderItem.getOrderItemId());
    }
}

