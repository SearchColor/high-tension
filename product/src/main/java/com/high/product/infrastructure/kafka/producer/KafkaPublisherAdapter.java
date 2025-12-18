package com.high.product.infrastructure.kafka.producer;

import org.springframework.stereotype.Component;

import com.high.product.application.dto.kafka.failure.StockRestoreFailMessage;
import com.high.product.application.dto.kafka.success.StockRestoreSuccessMessage;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class KafkaPublisherAdapter implements StockPublisherPort {

	private final ProductKafkaPublisher kafkaPublisher;

	@Override
	public void publishSuccess(StockDeductionSuccessMessage message) {
		kafkaPublisher.publishStockDeductionSuccess(message);
	}

	@Override
	public void publishFail(StockDeductionFailMessage message) {
		kafkaPublisher.publishStockDeductionFail(message);
	}

	@Override
	public void publishSuccess(StockRestoreSuccessMessage message) {
		kafkaPublisher.publishStockRestoreSuccess(message);
	}

	@Override
	public void publishFail(StockRestoreFailMessage message) {
		kafkaPublisher.publishStockRestoreFail(message);
	}
}