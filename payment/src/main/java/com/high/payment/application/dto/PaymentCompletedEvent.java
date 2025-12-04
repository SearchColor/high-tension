package com.high.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent (
	UUID paymentId,
	UUID orderId,
	UUID userId,
	BigDecimal amount,
	String finalStatus
) {}
