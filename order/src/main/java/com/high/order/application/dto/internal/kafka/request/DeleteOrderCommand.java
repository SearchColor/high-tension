package com.high.order.application.dto.internal.kafka.request;

import java.util.UUID;

public record DeleteOrderCommand(
    UUID sagaId,
    UUID orderId
) {

}
