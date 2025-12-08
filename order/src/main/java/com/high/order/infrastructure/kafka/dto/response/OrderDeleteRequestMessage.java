package com.high.order.infrastructure.kafka.dto.response;

import java.util.UUID;

public record OrderDeleteRequestMessage(
    UUID sagaId,
    UUID orderId
) {

}
