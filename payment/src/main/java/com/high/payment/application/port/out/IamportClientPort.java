package com.high.payment.application.port.out;

import com.high.payment.application.dto.IamportPaymentInfo;
import java.math.BigDecimal;
import java.util.UUID;

public interface IamportClientPort {
	// 실제 결제 요청을 보내고 PG사 거래 ID(imp_uid)를 반환받음
	String requestPayment(UUID orderId, BigDecimal amount, String method);

	IamportPaymentInfo getPaymentInfo(String impUid);

	// 금액 불일치 시 PG사에 취소 요청
	void cancelPayment(String impUid, BigDecimal amount,String reason);

	IamportPaymentInfo getPaymentInfoByMerchantUid(String merchantUid);
}
