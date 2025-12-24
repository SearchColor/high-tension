package com.high.product.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductKafkaPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void send(String topic, String sagaId, Object message) {
		try {
			// 수정 부분: 이미 String(JSON)인 경우와 객체인 경우를 구분
			String json;
			if (message instanceof String) {
				json = (String)message;
			} else {
				json = objectMapper.writeValueAsString(message);
			}

			kafkaTemplate.send(topic, sagaId, json);

			log.info(
				"[KafkaPublisher] topic={}, sagaId={}, message={}",
				topic, sagaId, json
			);

		} catch (Exception e) {
			log.error("[KafkaPublisher] 메시지 전송 실패", e);
			throw new RuntimeException(e);
		}
	}
}


