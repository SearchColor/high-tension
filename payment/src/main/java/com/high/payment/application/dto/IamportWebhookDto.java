package com.high.payment.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record IamportWebhookDto (

	@Schema(description = "Iamport 결제 고유 ID", example = "imp_success_123")
	@JsonProperty("imp_uid")
	String impUid, // 아임포트 거래 고유 번호

	@Schema(description = "가맹점 주문 ID(우리 시스템 orderId)", example = "11111111-1111-1111-1111-111111111116")
	@JsonProperty("merchant_uid")
	String merchantUid, // 상점 거래 고유 번호 (우리의 orderId 또는 paymentId가 될 수 있음)

	@Schema(description = "결제 상태", example = "paid")
	String status
) {
}
