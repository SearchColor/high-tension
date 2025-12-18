package com.high.payment.domain.model;

public enum PaymentStatus {
	PENDING,   // 결제 의도만 생성된 상태
	COMPLETED,   // PG에서 결제 승인 완료
	FAILED,      // 결제 실패
	CANCELED,
	REFUNDED     // 환불 완료 (나중에 사용)
}
