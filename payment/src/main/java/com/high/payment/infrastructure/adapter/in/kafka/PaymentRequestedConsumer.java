package com.high.payment.infrastructure.adapter.in.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.PaymentCreateCommandRequest;
import com.high.payment.application.service.PaymentService;
import com.high.payment.infrastructure.context.MessageContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestedConsumer {
	private final PaymentService paymentService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "payment-create-request", groupId = "payment-group")
	public void consume(ConsumerRecord<String, String> record) {
		try {
			log.info("Kafka Message Received: {}", record.value());

			//1) 오케스트레이션 메세지는 sagaId가 들어있는 Command 형태로 받음
			PaymentCreateCommandRequest command = objectMapper.readValue(record.value(), PaymentCreateCommandRequest.class);

			// 2) 컨텍스트 세팅
			MessageContext.set(command.userId(), "USER");

			// 3) createPayment()가 아니라, saga 진입점으로 처리
			paymentService.processPaymentSaga(command);


		} catch (Exception e) {
			log.error("Kafka 메시지 처리 중 오류 발생", e);
			// 추후 DLQ(Dead Letter Queue) 처리 로직 필요
		} finally {
			MessageContext.clear();
		}
	}
}
