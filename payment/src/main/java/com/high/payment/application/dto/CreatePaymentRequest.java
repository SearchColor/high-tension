package com.high.payment.application.dto;

import java.util.UUID;

public record CreatePaymentRequest (
	UUID orderId,
	UUID userId,
	Integer paymentPrice,
	String paymentMethod
) {}