package com.high.order.application.dto.response;

import com.high.order.domain.entity.Order;
import java.util.UUID;

public record OrderResponse(
    UUID orderId
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getOrderId());
    }
}
