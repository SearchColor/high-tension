package com.high.order.application.dto.response;

import java.util.UUID;

public record OrderItemCancelResponse(
    UUID orderItemId
) {
    public static OrderItemCancelResponse of(UUID orderItemId) {
        return new OrderItemCancelResponse(orderItemId);
    }
}
