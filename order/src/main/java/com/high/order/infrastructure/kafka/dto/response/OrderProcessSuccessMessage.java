package com.high.order.infrastructure.kafka.dto.response;

import java.util.UUID;

public record OrderProcessSuccessMessage(
    UUID sagaId,
    UUID orderId,
    UUID userId
) {

}
