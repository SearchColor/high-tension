package com.high.order.application.dto.response;

import com.high.order.domain.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
    UUID orderId,
    UUID userId,
    UUID couponId,
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
    public static OrderDetailResponse from(Order order) {

        return new OrderDetailResponse(
            order.getOrderId(),
            order.getCustomerId(),
            order.getCouponId(),
            order.getTotalPrice(),
            order.getDiscountAmount(),
            order.getPaidAmount(),
            order.getOrderStatus().toString(),
            order.getRecipient(),
            order.getRecipientContact(),
            order.getDetailAddress(),
            order.getDetailAddress(),
            order.getRequestMessage(),
            LocalDateTime.now(), //BaseEntity적용 전 임시
            order.getOrderItems().stream().map(OrderItemResponse::from).toList()

        );
    }
}
