package com.high.payment.infrastrcutre.kafka.dto;

import java.util.UUID;

public record PaymentCreateSuccessMessage (
	UUID sagaId,
	UUID orderId,
	UUID userId
) {

}