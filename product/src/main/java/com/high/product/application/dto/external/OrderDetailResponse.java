package com.high.product.application.dto.external;

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
}
