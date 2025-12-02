package com.high.order.application.dto.response;

import com.high.order.domain.entity.Order;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderListResponse(
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
    LocalDateTime createdAt
) {
    public static OrderListResponse from(Order order) {

        return new OrderListResponse(
            order.getOrderId(),
            order.getCustomerId(),
            order.getCouponId(),
            order.getTotalPrice(),
            order.getDiscountAmount(),
            order.getPaidAmount(),
            order.getOrderStatus().toString(),
            order.getRecipient(),
            order.getRecipientContact(),
            order.getDeliveryAddress(),
            order.getDetailAddress(),
            order.getRequestMessage(),
            order.getCreatedAt()
        );
    }
}
