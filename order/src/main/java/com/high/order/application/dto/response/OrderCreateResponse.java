package com.high.order.application.dto.response;

import com.high.order.domain.entity.Order;
import java.util.UUID;

public record OrderCreateResponse(
    UUID orderId
) {

    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(order.getOrderId());
    }
}
