package com.high.payment.application.dto;


public interface PaymentSagaEventPort {
	void publishPaymentResult(PaymentSagaResultMessage result);
}
