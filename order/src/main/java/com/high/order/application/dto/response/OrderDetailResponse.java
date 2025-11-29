package com.high.order.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
    UUID orderId,
    UUID userId,
    UUID couponID,
    Integer totalAmount,
    Integer discountAmount,
    Integer paidAmount,
    String orderStatus,
    String recipient,
    String recipientContact,
    String deliveryAddress,
    String detailAddress,
    String requestMessage,
    LocalDateTime createdAt,
    List<OrderItemResponse> orderItems
) {
//    public static OrderDetailResponse from(Order order) {
//        return new OrderDetailResponse(
//            result.orderId(),
//            result.customerId(),
//            result.status(),
//            result.items().stream().map(OrderItemResponse::from).toList(),
//            result.totalAmount(),
//            result.couponCode(),
//            result.discountAmount(),
//            result.finalAmount(),
//            result.cancellable(),
//            result.orderedAt()
//        );
//    }
}
