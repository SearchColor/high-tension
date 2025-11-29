package com.high.order.application.dto.response;

import java.util.UUID;

public record OrderItemResponse(
    UUID orderItemId,
    UUID productId,
    UUID producerId,
    Integer unitPrice,
    Integer quantity,
    Integer totalPrice,
    String orderItemStatus,
    String deliveryStatus
) {
//    public static OrderItemResponse from(OrderItemResult result) {
//        return new OrderItemResponse(
//            result.productId(),
//            result.productName(),
//            result.price(),
//            result.quantity(),
//            result.totalPrice()
//        );
//    }
}
