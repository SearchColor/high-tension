package com.high.payment.infrastrcutre.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.high.payment.application.dto.PaymentCreateCommandRequest;
import com.high.payment.application.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandConsumer {
	private final PaymentService paymentService;

	@KafkaListener(topics = "payment-create-request", groupId = "payment-service-saga-group")
	public void handlePaymentCommand(PaymentCreateCommandRequest request) {
		log.info("[SAGA] 결제 Command 수신. SagaId={}, OrderId={}",
				 request.sagaId(), request.orderId());

		try {
			paymentService.processPaymentSaga(request);


		} catch (Exception e) {
			log.error("[SAGA] 결제 처리 중 알 수 없는 오류 발생. SagaId: {}", request.sagaId(), e);
		}
	}
}
