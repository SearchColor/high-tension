package com.high.order.application.dto.event.response;

import com.high.order.domain.entity.Order;
import java.util.UUID;

public record OrderSuccessResponse(
    UUID sagaId,
    UUID orderId,
    UUID userId

) {
    public static OrderSuccessResponse of(Order order, UUID sagaId) {
        return new OrderSuccessResponse(
            sagaId,
            order.getOrderId(),
            order.getCustomerId()

        );
    }
}
