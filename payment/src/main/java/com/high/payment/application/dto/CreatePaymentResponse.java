package com.high.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Outbox에 저장할 데이터
public record CreatePaymentResponse (
	UUID paymentId,
	UUID orderId,
	UUID userId,
	BigDecimal amount,
	String status
) {}
