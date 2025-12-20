package com.high.payment.application.dto;

import java.util.UUID;

public record PaymentCompletedEvent (
	UUID paymentId,
	UUID orderId,
	UUID userId,
	Integer paymentPrice,
	String finalStatus
) {}
