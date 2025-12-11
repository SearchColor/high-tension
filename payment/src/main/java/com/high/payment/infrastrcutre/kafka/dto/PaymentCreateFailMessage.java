package com.high.payment.infrastrcutre.kafka.dto;

import java.util.UUID;

public record PaymentCreateFailMessage (
	UUID sagaId,
	UUID orderId,
	String message,
	UUID userId
) {

}
