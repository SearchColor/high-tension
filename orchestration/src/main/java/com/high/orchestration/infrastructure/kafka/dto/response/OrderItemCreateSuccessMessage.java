package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.UUID;

public record OrderItemCreateSuccessMessage(
    UUID productId,
    Integer quantity
) {

}
