package com.high.product.infrastructure.kafka.messaging.request;

import java.util.List;
import java.util.UUID;

public record StockReduceRequest(
	UUID couponId,
	UUID sagaId,
	UUID orderId,
	UUID ordererId,
	String recipient,
	String recipientContact,
	String deliveryAddress,
	String detailAddress,
	String requestMessage,
	List<StockReduceItem> itemList
) {}