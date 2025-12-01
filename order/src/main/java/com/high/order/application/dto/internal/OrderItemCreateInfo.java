package com.high.order.application.dto.internal;

import java.util.UUID;

public record OrderItemCreateInfo(
    UUID productId,
    UUID producerId,
    Integer unitPrice,
    Integer quantity
) {

}
