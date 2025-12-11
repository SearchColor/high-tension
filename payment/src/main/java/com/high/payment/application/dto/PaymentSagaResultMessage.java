package com.high.payment.application.dto;

import java.util.UUID;

public record PaymentSagaResultMessage (
	UUID sagaId,
	UUID orderId,
	UUID userId,
	boolean success,
	String message,
	Integer errorCode
) {}