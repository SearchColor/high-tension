package com.high.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.high.payment.domain.model.Payment;

// Outbox에 저장할 데이터
public record CreatePaymentResponse (
	UUID paymentId,
	UUID orderId,
	String userId,
	BigDecimal amount,
	String status
) {
	public static CreatePaymentResponse from(Payment payment) {
		return new CreatePaymentResponse(
			payment.getId(),
			payment.getOrderId(),
			payment.getUserId(),
			payment.getAmount(),
			payment.getStatus().name()
		);
	}


}
