package com.high.payment.infrastructure.adapter.out.client;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaProducerAdapter implements KafkaProducerPort {
	@Override
	public void send(String topic, String key, String payload) {
		log.info("[KAFKA MOCK] 이벤트 발행: Topic={}, Key={}, Payload={}",
				 topic, key, payload.substring(0, Math.min(payload.length(), 100)) + "...");
	}
}
