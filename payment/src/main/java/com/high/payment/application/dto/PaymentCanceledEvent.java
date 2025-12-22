package com.high.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCanceledEvent (
	UUID paymentId,
	UUID orderId,
	UUID userId,
	Integer paymentPrice,
	String reason
) {}

