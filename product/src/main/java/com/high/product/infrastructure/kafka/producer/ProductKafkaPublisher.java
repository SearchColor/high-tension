package com.high.product.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.product.infrastructure.kafka.messaging.failure.StockDeductionFailMessage;
import com.high.product.infrastructure.kafka.messaging.success.StockDeductionSuccessMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductKafkaPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void publishStockDeductionSuccess(StockDeductionSuccessMessage message) {
		send("stock-deduction-success", message);
	}

	public void publishStockDeductionFail(StockDeductionFailMessage message) {
		send("stock-deduction-fail", message);
	}

	private void send(String topic, Object message) {
		try {
			String json = objectMapper.writeValueAsString(message);
			kafkaTemplate.send(topic, json);
			log.info("[KafkaPublisher] topic={}, message={}", topic, json);
		} catch (Exception e) {
			log.error("[KafkaPublisher] Kafka 메시지 직렬화 실패: {}", message, e);
			throw new RuntimeException(e);
		}
	}
}

