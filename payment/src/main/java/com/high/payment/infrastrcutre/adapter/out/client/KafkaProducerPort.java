package com.high.payment.infrastrcutre.adapter.out.client;

public interface KafkaProducerPort {
	void send(String topic, String key, String payload);
}
