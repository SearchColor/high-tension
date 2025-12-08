package com.high.payment.application.service;

import java.util.UUID;

import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.CreatePaymentResponse;

public interface PaymentService {
	void processPayment(CreatePaymentRequest event);

	CreatePaymentResponse createPayment(CreatePaymentRequest request);

	CreatePaymentResponse getPayment(UUID paymentId);

	void verifyAndFinalizePayment(String impUid, String merchantUid);

	void cancelPayment(UUID orderId);
}
