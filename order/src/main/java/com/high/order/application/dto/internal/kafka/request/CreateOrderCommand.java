package com.high.order.application.dto.internal.kafka.request;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
    UUID couponIssueId,
    UUID sagaId,
    UUID orderId,
    UUID ordererId,
    String recipient,
    String recipientContact,
    String deliveryAddress,
    String detailAddress,
    String requestMessage,
    List<CreateOrderItemCommand> itemList
) {

}
