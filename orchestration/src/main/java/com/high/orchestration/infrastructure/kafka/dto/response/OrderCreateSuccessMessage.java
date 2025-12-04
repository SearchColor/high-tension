package com.high.orchestration.infrastructure.kafka.dto.response;

import java.util.List;
import java.util.UUID;

public record OrderCreateSuccessMessage(
    UUID orderId,
    UUID sagaId,
    UUID couponId,
    UUID ordererId,
    List<OrderItemCreateSuccessMessage> itemList
) {

}
