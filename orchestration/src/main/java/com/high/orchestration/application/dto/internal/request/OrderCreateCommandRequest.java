package com.high.orchestration.application.dto.internal.request;

import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.high.orchestration.application.dto.request.OrderItemRequest;
import java.util.List;
import java.util.UUID;

public record OrderCreateCommandRequest(
    UUID couponIssueId,
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
    public static OrderCreateCommandRequest from(UUID orderId, UUID sagaId, UUID ordererId, OrderCreateRequest orderCreateRequest) {
        return new OrderCreateCommandRequest(
            orderCreateRequest.couponId(),
            sagaId,
            orderId,
            ordererId,
            orderCreateRequest.recipient(),
            orderCreateRequest.recipientContact(),
            orderCreateRequest.deliveryAddress(),
            orderCreateRequest.detailAddress(),
            orderCreateRequest.requestMessage(),
            orderCreateRequest.itemList()
        );
    }
}
