package com.high.payment.application.dto.external;

import java.util.UUID;

public record OrderDetailResponse(
	UUID orderId,
	Integer totalPrice,
	Integer paidAmount
) {}
