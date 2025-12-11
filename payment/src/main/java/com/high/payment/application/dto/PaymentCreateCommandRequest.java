package com.high.payment.application.dto;

import java.util.UUID;

public record PaymentCreateCommandRequest(
	UUID sagaId,
	UUID orderId,
	UUID userId
) {

}
