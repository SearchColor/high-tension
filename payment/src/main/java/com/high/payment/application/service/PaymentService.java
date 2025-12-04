package com.high.payment.application.service;

import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.CreatePaymentResponse;

public interface PaymentService {
	void processPayment(CreatePaymentRequest event);

	CreatePaymentResponse createPayment(CreatePaymentRequest request);
}
