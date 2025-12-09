package com.high.order.application.dto.internal.kafka.request;

import java.util.UUID;

public record ProcessOrderSuccessCommand(
    UUID sagaId,
    UUID orderId
) {

}
