package com.high.product.application.dto.kafka.dlq;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockRestoreDlqMessage(
	UUID sagaId,
	UUID orderId,
	UUID userId,

	String originalTopic,
	int retryCount,
	String exceptionType,
	String exceptionMessage,
	String stackTrace,

	String consumerGroup,
	LocalDateTime failedAt
) {
}
