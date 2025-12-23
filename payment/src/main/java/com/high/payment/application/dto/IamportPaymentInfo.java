package com.high.payment.application.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class IamportPaymentInfo {
	private final String impUid;        // PG사 고유 결제 ID
	private final String merchantUid;   // 가맹점 주문 ID (우리측 OrderId)
	private final Integer paymentPrice;    // 실제 결제된 금액 (정합성 검증 대상)
	private final String pgTid;         // PG사 거래 번호 (DB에 저장)
	private final String status;
}
