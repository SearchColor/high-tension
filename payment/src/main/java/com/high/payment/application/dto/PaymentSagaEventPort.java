package com.high.payment.application.dto;

import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateFailMessage;

public interface PaymentSagaEventPort {
	void publishPaymentResult(PaymentSagaResultMessage result);
}
