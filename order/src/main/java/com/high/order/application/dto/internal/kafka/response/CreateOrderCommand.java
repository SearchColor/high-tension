package com.high.order.application.dto.internal.kafka.response;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
    UUID couponId,
    UUID sagaId,
    UUID orderId,
    String recipient,
    String recipientContact,
    String deliveryAddress,
    String detailAddress,
    String requestMessage,
    List<CreateOrderItemCommand> itemList
) {

}
