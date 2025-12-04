package com.high.order.infrastructure.kafka.dto.response;

import com.high.order.application.dto.request.OrderItemRequest;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequestMessage(
    UUID couponId,
    UUID sagaId,
    UUID orderId,
    UUID ordererId,
    String recipient,
    String recipientContact,
    String deliveryAddress,
    String detailAddress,
    String requestMessage,
    List<OrderItemRequest> itemList
) {

}
