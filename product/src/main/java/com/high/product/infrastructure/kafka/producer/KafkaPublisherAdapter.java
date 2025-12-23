package com.high.product.infrastructure.kafka.producer;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.product.application.dto.kafka.failure.StockRestoreFailMessage;
import com.high.product.application.dto.kafka.success.StockRestoreSuccessMessage;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;
import com.high.product.domain.model.KafkaOutbox;
import com.high.product.domain.repository.KafkaOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPublisherAdapter implements StockPublisherPort {

	private final ProductKafkaPublisher kafkaPublisher;
	private final KafkaOutboxRepository kafkaOutboxRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void publishSuccess(StockDeductionSuccessMessage message) {
		kafkaPublisher.send(
			"stock-deduction-success",
			String.valueOf(message.sagaId()),
			message
		);

		saveOutbox(
			"stock-deduction-success",
			message.sagaId(), message.orderId(),
			message.userId(), message);
	}

	@Override
	public void publishFail(StockDeductionFailMessage message) {
		kafkaPublisher.send(
			"stock-deduction-fail",
			String.valueOf(message.sagaId()),
			message
		);

		saveOutbox("stock-deduction-fail",
			message.sagaId(), message.orderId(),
			message.userId(),
			message);
	}

	@Override
	public void publishSuccess(StockRestoreSuccessMessage message) {
		kafkaPublisher.send(
			"stock-restore-success",
			String.valueOf(message.sagaId()),
			message
		);

		saveOutbox("stock-restore-success",
			message.sagaId(), message.orderId(),
			message.userId(),
			message);
	}

	@Override
	public void publishFail(StockRestoreFailMessage message) {
		kafkaPublisher.send(
			"stock-restore-fail",
			String.valueOf(message.sagaId()),
			message
		);

		saveOutbox("stock-restore-fail",
			message.sagaId(),
			message.orderId(),
			message.userId(),
			message);

	}

	// 아웃박스 경유 발행용 메서드
	public void saveOutbox(String topic, UUID sagaId, UUID orderId, UUID userId, Object message) {
		try {
			// 객체를 JSON 문자열로 변환
			String jsonPayload = objectMapper.writeValueAsString(message);

			KafkaOutbox outbox = KafkaOutbox.builder()
				.topic(topic)
				.messageKey(sagaId.toString())
				.payload(jsonPayload)
				.status("PENDING")
				.retryCount(0)
				.sagaId(sagaId)
				.orderId(orderId)
				.userId(userId)
				.build();

			kafkaOutboxRepository.save(outbox);
			log.info("[Outbox] 저장 완료 - sagaId: {}", sagaId);

		} catch (JsonProcessingException e) {
			log.error("[Outbox] 직렬화 실패 - sagaId: {}", sagaId);
			throw new IllegalArgumentException("메시지 직렬화 오류", e);
		} catch (Exception e) {
			log.error("[Outbox] 시스템 오류 - sagaId: {}", sagaId, e);
			throw new RuntimeException("아웃박스 처리 중 예외 발생", e);
		}
	}
}
