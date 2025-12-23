package com.high.payment.infrastructure.adapter.in.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.dto.CreatePaymentRequest;
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
			CreatePaymentRequest event = objectMapper.readValue(record.value(), CreatePaymentRequest.class);

			MessageContext.set(event.userId(), "USER");

			paymentService.processPayment(event);

		} catch (Exception e) {
			log.error("Kafka 메시지 처리 중 오류 발생", e);
			// 추후 DLQ(Dead Letter Queue) 처리 로직 필요
		} finally {
			MessageContext.clear();
		}
	}
}
