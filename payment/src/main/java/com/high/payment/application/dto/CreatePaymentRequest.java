package com.high.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest (
	UUID orderId,
	String userId,
	BigDecimal amount,
	String paymentMethod
) {}