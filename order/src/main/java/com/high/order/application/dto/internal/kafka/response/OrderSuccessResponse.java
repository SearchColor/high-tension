package com.high.order.application.dto.internal.kafka.response;

import com.high.order.domain.entity.Order;
import java.util.List;
import java.util.UUID;

public record OrderSuccessResponse(
    UUID orderId,
    UUID sagaId,
    UUID couponId,
    UUID ordererId,
    List<OrderItemSuccessResponse> itemList
) {
    public static OrderSuccessResponse of(Order order, UUID sagaId, UUID ordererId) {
        return new OrderSuccessResponse(
            order.getOrderId(),
            sagaId,
            order.getCouponId(),
            ordererId,
            order.getOrderItems().stream().map(OrderItemSuccessResponse::from).toList()
        );
    }
}
