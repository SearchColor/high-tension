package com.high.payment.infrastructure.adapter.out.client;

public interface KafkaProducerPort {
	void send(String topic, String key, String payload);
}
