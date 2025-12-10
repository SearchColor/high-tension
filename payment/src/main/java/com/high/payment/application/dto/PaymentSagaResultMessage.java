package com.high.payment.application.dto;

import java.util.UUID;

public record PaymentSagaResultMessage (
	UUID sagaId,
	UUID orderId,
	boolean success,
	String message,
	Integer errorCode
) {}