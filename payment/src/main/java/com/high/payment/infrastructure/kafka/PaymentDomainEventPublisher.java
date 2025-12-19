package com.high.payment.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.high.payment.domain.model.PaymentOutbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentDomainEventPublisher {
	private final KafkaTemplate<String, String> kafkaTemplate;

	// Outbox.topic에 넣은 값으로 보내는 구조면 토픽 상수 불필요
	// 토픽을 고정하고 싶으면 상수로 박아도 됨.
	public void publish(PaymentOutbox outbox) {
		String topic = outbox.getTopic();          // 예: "payment-events" 또는 "order-events"
		String key = outbox.getMessageKey();       // aggregateId 기반 추천
		String payload = outbox.getPayload();      // JSON String

		kafkaTemplate.send(topic, key, payload);
		log.info("[Kafka] published. topic={}, key={}, eventType={}", topic, key, outbox.getEventType());
	}
}
