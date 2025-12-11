package com.high.order.application.dto.response;

import com.high.order.domain.entity.Order;
import com.high.order.domain.entity.OrderItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
    UUID orderId,
    UUID userId,
    UUID couponIssueId,
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
            order.getCouponIssueId(),
            order.getTotalPrice(),
            order.getDiscountAmount(),
            order.getPaidAmount(),
            order.getOrderStatus().toString(),
            order.getRecipient(),
            order.getRecipientContact(),
            order.getDeliveryAddress(),
            order.getDetailAddress(),
            order.getRequestMessage(),
            order.getCreatedAt(),
            order.getOrderItems().stream().map(OrderItemResponse::from).toList()

        );
    }

    public static OrderDetailResponse seller(Order order, List<OrderItem> orderItems) {

        return new OrderDetailResponse(
            order.getOrderId(),
            order.getCustomerId(),
            order.getCouponIssueId(),
            order.getTotalPrice(),
            order.getDiscountAmount(),
            order.getPaidAmount(),
            order.getOrderStatus().toString(),
            order.getRecipient(),
            order.getRecipientContact(),
            order.getDeliveryAddress(),
            order.getDetailAddress(),
            order.getRequestMessage(),
            order.getCreatedAt(),
            orderItems.stream().map(OrderItemResponse::from).toList()
        );
    }
}
