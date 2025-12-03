package com.high.payment.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface IamportClientPort {
	// 실제 결제 요청을 보내고 PG사 거래 ID(imp_uid)를 반환받음
	String requestPayment(UUID orderId, BigDecimal amount, String method);
}
