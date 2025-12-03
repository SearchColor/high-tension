package com.high.payment.application.service;

import com.high.payment.application.dto.CreatePaymentRequest;

public interface PaymentService {
	void processPayment(CreatePaymentRequest event);
}
