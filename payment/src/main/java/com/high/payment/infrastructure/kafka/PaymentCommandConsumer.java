package com.high.payment.infrastructure.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.dto.PaymentCreateCommandRequest;
import com.high.payment.application.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandConsumer {
	private final PaymentService paymentService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "payment-create-request", groupId = "payment-service-saga-group")
	public void handlePaymentCommand(String message) {

		try {
			PaymentCreateCommandRequest request =
				objectMapper.readValue(message, PaymentCreateCommandRequest.class);
			paymentService.processPaymentSaga(request);


		} catch (Exception e) {
			log.error("[Consumer] payment-create-request 메시지 처리 실패. raw={}", message, e);
		}
	}
}
