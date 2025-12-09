package com.high.order.application.dto.internal.kafka.request;

import java.util.UUID;

public record CreateOrderFailCommand(
    UUID sagaId,
    String reason
) {

}
