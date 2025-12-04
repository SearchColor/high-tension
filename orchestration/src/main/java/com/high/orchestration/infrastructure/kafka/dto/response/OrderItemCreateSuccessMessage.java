package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.UUID;
//TODO: 필요없는 클래스인지 고려
public record OrderItemCreateSuccessMessage(
    UUID productId,
    Integer quantity
) {

}
