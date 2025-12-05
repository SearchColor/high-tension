package com.high.order.application.dto.internal.kafka.response;

import com.high.order.domain.entity.Order;
import java.util.UUID;

public record OrderSuccessResponse(
    UUID orderId,
    UUID sagaId

) {
    public static OrderSuccessResponse of(Order order, UUID sagaId) {
        return new OrderSuccessResponse(
            order.getOrderId(),
            sagaId
        );
    }
}
