package com.high.order.application.dto.internal.kafka.response;

import java.util.UUID;

public record OrderCreateFailedResponse(
    UUID sagaId,
    String reason
) {
    public static OrderCreateFailedResponse of(
        UUID sagaId,
        String reason
    ) {
        return new OrderCreateFailedResponse(sagaId, reason);
    }

}
