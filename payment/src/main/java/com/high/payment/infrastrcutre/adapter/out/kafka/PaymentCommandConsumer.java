package com.high.payment.infrastrcutre.adapter.out.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.payment.application.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandConsumer {

	private final PaymentService paymentService;
	private final ObjectMapper objectMapper;

	// Orchestration Service에서 발행한 결제 요청 Command를 구독합니다.
	private static final String TOPIC_COMMAND = "payment-create-request";

	@KafkaListener(topics = TOPIC_COMMAND)
	public void consumePaymentCreateCommand(String jsonMessage) {
		log.info("[KafkaConsumer] Payment Create Command 수신: {}", jsonMessage);

		PaymentCreateCommandRequest command;
		try {
			// 1. JSON 메시지를 DTO로 파싱
			command = objectMapper.readValue(jsonMessage, PaymentCreateCommandRequest.class);

			// 2. Saga 처리 로직 호출
			paymentService.processPaymentSaga(command);

			log.info("[KafkaConsumer] Payment Create Command 처리 완료. SagaId: {}", command.sagaId());

		} catch (Exception e) {
			log.error("[KafkaConsumer] 메시지 처리 실패 (파싱 오류 혹은 SAGA 내부 오류): {}", jsonMessage, e);

		}
	}
}
