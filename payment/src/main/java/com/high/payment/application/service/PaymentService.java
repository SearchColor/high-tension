package com.high.payment.application.service;

import java.util.UUID;

import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.CreatePaymentResponse;
import com.high.payment.application.dto.PaymentDetailResponse;

public interface PaymentService {
	void processPayment(CreatePaymentRequest event);

	CreatePaymentResponse createPayment(CreatePaymentRequest request);

	CreatePaymentResponse getPayment(UUID paymentId);

	PaymentDetailResponse getPaymentByOrderId(UUID orderId);

	void verifyAndFinalizePayment(String impUid, String merchantUid);

	void cancelPayment(UUID orderId);

	void processPaymentSaga(PaymentCreateCommandRequest sagaCommand);
}
