package com.high.product.application.dto.external;

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
){}
