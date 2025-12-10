package com.high.product.infrastructure.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.product.application.service.StockDeductionService;
import com.high.product.application.dto.kafka.request.StockDeductionCommandRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductionKafkaConsumer {

	private final StockDeductionService stockDeductionService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "stock-deduction-request")
	public void consumeStockDeductionRequest(String message) {
		try {
			StockDeductionCommandRequest request = objectMapper.readValue(message, StockDeductionCommandRequest.class);
			stockDeductionService.handleStockDeduction(request);
		} catch (Exception e) {
			log.error("[KafkaConsumer] stock-deduction-request 메시지 처리 실패: {}", message, e);
		}
	}
}
