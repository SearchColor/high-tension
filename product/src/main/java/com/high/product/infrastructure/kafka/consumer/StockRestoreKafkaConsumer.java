package com.high.product.infrastructure.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.product.application.dto.kafka.request.StockRestoreCommandRequest;
import com.high.product.application.service.StockRestoreService;
import com.high.product.infrastructure.context.MessageContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRestoreKafkaConsumer {

	private final StockRestoreService stockRestoreService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "stock-restore-request")
	public void consumeStockRestoreRequest(String message) {
		try {
			StockRestoreCommandRequest request = objectMapper.readValue(message, StockRestoreCommandRequest.class);

			// Kafka 메시지 기반 ThreadLocal 세팅
			MessageContext.set(request.userId(), "USER");

			stockRestoreService.handleStockRestore(request);
		} catch (Exception e) {
			log.error("[KafkaConsumer] stock-restore-request 메시지 처리 실패: {}", message, e);
		} finally {
			// 처리 후 ThreadLocal 정리
			MessageContext.clear();
		}
	}
}
