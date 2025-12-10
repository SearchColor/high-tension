package com.high.product.infrastructure.kafka.producer;

import org.springframework.stereotype.Component;

import com.high.product.application.port.StockDeductionPublisherPort;
import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class KafkaPublisherAdapter implements StockDeductionPublisherPort {

	private final ProductKafkaPublisher kafkaPublisher;

	@Override
	public void publishSuccess(StockDeductionSuccessMessage message) {
		kafkaPublisher.publishStockDeductionSuccess(message);
	}

	@Override
	public void publishFail(StockDeductionFailMessage message) {
		kafkaPublisher.publishStockDeductionFail(message);
	}
}