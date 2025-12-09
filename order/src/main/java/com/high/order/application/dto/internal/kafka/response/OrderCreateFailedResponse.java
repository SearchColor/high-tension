package com.high.order.application.dto.internal.kafka.response;

import java.util.UUID;

public record OrderCreateFailedResponse(
    UUID sagaId,
    String reason,
    UUID userId
) {
    public static OrderCreateFailedResponse of(
        UUID sagaId,
        String reason,
        UUID userId
    ) {
        return new OrderCreateFailedResponse(sagaId, reason, userId);
    }

}
