package com.high.order.application.dto.internal.kafka.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProcessOrderSuccessCommand(
    UUID orderId,
    UUID userId,
    UUID paidAmount,
    LocalDateTime createdAt,
    List<String> orderItemNameList
) {

}
