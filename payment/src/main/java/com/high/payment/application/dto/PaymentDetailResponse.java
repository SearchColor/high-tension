package com.high.payment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.high.payment.domain.model.Payment;

public record PaymentDetailResponse (
	String orderId,
	String paymentId,
	BigDecimal amount,
	String status, // PaymentStatus
	String pgTid,
	LocalDateTime createdAt
) {
	public static PaymentDetailResponse from(Payment payment) {
		return new PaymentDetailResponse(
			payment.getOrderId().toString(),
			payment.getId().toString(),
			payment.getAmount(),
			payment.getStatus().name(),
			payment.getPgTid(),
			payment.getCreatedAt()
		);
	}
}
