package com.high.order.application.dto.event.response;

import java.util.UUID;

public record OrderDeleteResponse(
    UUID sagaId,
    UUID orderId,
    String resultMessage

) {
    public static OrderDeleteResponse of (
        UUID sagaId,
        UUID orderId,
        String resultMessage
    ) {
        return new OrderDeleteResponse(sagaId, orderId, resultMessage);
    }
}
