package com.high.product.infrastructure.kafka.producer;

import org.springframework.stereotype.Component;

import com.high.product.application.dto.kafka.failure.StockRestoreFailMessage;
import com.high.product.application.dto.kafka.success.StockRestoreSuccessMessage;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaPublisherAdapter implements StockPublisherPort {

	private final ProductKafkaPublisher kafkaPublisher;

	@Override
	public void publishSuccess(StockDeductionSuccessMessage message) {
		kafkaPublisher.send(
			"stock-deduction-success",
			String.valueOf(message.sagaId()),
			message
		);
	}

	@Override
	public void publishFail(StockDeductionFailMessage message) {
		kafkaPublisher.send(
			"stock-deduction-fail",
			String.valueOf(message.sagaId()),
			message
		);
	}

	@Override
	public void publishSuccess(StockRestoreSuccessMessage message) {
		kafkaPublisher.send(
			"stock-restore-success",
			String.valueOf(message.sagaId()),
			message
		);
	}

	@Override
	public void publishFail(StockRestoreFailMessage message) {
		kafkaPublisher.send(
			"stock-restore-fail",
			String.valueOf(message.sagaId()),
			message
		);
	}
}
