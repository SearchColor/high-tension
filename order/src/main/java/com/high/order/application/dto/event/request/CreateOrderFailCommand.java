package com.high.order.application.dto.event.request;

import java.util.UUID;

public record CreateOrderFailCommand(
    UUID sagaId,
    String reason
) {

}
