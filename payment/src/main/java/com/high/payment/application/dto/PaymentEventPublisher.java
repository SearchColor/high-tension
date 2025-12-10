package com.high.payment.application.dto;

import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateFailMessage;

public interface PaymentEventPublisher {
	// 성공 이벤트 발행
	void publishSuccess(PaymentCreateSuccessMessage message);

	// 실패 이벤트 발행
	void publishFail(PaymentCreateFailMessage message);

}
