package com.high.order.application.dto.event.request;

import java.util.UUID;

public record DeleteOrderCommand(
    UUID sagaId,
    UUID orderId,
    UUID userId
) {

}
