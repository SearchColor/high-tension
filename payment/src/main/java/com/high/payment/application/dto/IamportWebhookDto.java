package com.high.payment.application.dto;

public record IamportWebhookDto (
	String impUid, // 아임포트 거래 고유 번호
	String merchantUid, // 상점 거래 고유 번호 (우리의 orderId 또는 paymentId가 될 수 있음)
	String status // 결제 상태 (ready, paid, cancelled, failed)
) {
}
