package com.high.product.infrastructure.kafka.consumer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.product.application.dto.kafka.dlq.StockRestoreDlqMessage;
import com.high.product.application.dto.kafka.request.StockRestoreCommandRequest;
import com.high.product.application.service.StockRestoreService;
import com.high.product.domain.model.KafkaOutbox;
import com.high.product.domain.repository.KafkaOutboxRepository;
import com.high.product.application.port.SagaDeduplicationPort;
import com.high.product.infrastructure.context.MessageContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRestoreKafkaConsumer {

	private final StockRestoreService stockRestoreService;
	private final ObjectMapper objectMapper;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final SagaDeduplicationPort sagaDeduplicationPort;
	private final KafkaOutboxRepository kafkaOutboxRepository;

	/**
	 *
	 * - attempts = 3: 첫 시도 포함 총 3번 시도
	 * - backoff: 1초 시작, 2배씩 증가, 최대 10초 대기 (지수 백오프)
	 * - dltTopicSuffix: 실패 시 '-dlq' 토픽으로 이동, KafkaConfig 설정
	 */
	@RetryableTopic
	@KafkaListener(
		topics = "stock-restore-request",
		groupId = "product-service-group"
	)
	public void consume(String message) throws Exception {
		log.info("Received stock restore message: {}", message);

		// 메시지 역직렬화
		StockRestoreCommandRequest request =
			objectMapper.readValue(message, StockRestoreCommandRequest.class);

		// 컨텍스트 설정 (ThreadLocal)
		MessageContext.set(request.userId(), "USER");

		try {
			// 멱등성 체크: 이미 처리된 saga 재처리 방지
			if (sagaDeduplicationPort.exists("success:" + request.sagaId()) ||
				sagaDeduplicationPort.exists("fail:" + request.sagaId())) {
				log.info("이미 처리된 sagaId={}, 재처리 스킵", request.sagaId());
				return;
			}

			// 재고 복구 서비스 호출
			stockRestoreService.handleStockRestore(request);
			log.info(
				"재고 복구 이벤트 성공 후 sagaId={} orderId={} ,userId={}",
				request.sagaId(),
				request.orderId(),
				request.userId()
			);

		} catch (Exception e) {
			log.error("재고 복구 이벤트 처리 도중 예외 발생: {}", e.getMessage());
			// 예외를 다시 던져야 @RetryableTopic이 감지하여 재시도 스케줄링을 진행함
			throw e;

		} finally {
			// ThreadLocal 자원 해제
			MessageContext.clear();
		}
	}

	// 재시도 최종 실패 시, 해당 이벤트 DLQ로 이동
	@DltHandler
	@Transactional
	public void handleDlt(String message,
		Exception e,
		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		@Header(KafkaHeaders.OFFSET) long offset,
		@Header(KafkaHeaders.GROUP_ID) String consumerGroup) {

		try {

			String stackTrace = getStackTraceAsString(e);

			StockRestoreCommandRequest request = objectMapper.readValue(message, StockRestoreCommandRequest.class);

			StockRestoreDlqMessage dlqMessage = new StockRestoreDlqMessage(
				request.sagaId(),
				request.orderId(),
				request.userId(),
				topic,
				0, // 재시도 횟수는 @RetryableTopic에 의해 수행됨
				e.getClass().getSimpleName(),
				e.getMessage(),
				stackTrace,
				consumerGroup,
				LocalDateTime.now()
			);
			// Todo: 운영팀 알림(Slack 등)
			log.error("[DLQ] 재고 복구 최종 재시도 실패, DLQ로 이동: {}", dlqMessage);

			// 멱등성 보관소에 최종 실패 기록 (30일 보관)
			sagaDeduplicationPort.save("fail:" + request.sagaId(), 86400 * 30);

			// 아웃박스 테이블에 최종 실패 상태 저장
			try {
				String payload = objectMapper.writeValueAsString(request);
				KafkaOutbox outbox = KafkaOutbox.builder()
					.topic("stock-restore-dlq")
					.payload(payload)
					.status("FAILED")
					.sagaId(request.sagaId())
					.orderId(request.orderId())
					.userId(request.userId())
					.build();
				kafkaOutboxRepository.save(outbox);
				log.info("[DLQ][Outbox] 재고 복구 FAILED 메시지 저장 완료 sagaId={}", request.sagaId());
			} catch (Exception exOutbox) {
				log.error("[DLQ][Outbox] FAILED 메시지 저장 실패 sagaId={}", request.sagaId(), exOutbox);
			}

		} catch (Exception ex) {
			log.error("DLQ 메시지 생성 실패: {}", ex.getMessage(), ex);
		}
	}

	private String getStackTraceAsString(Throwable throwable) {
		if (throwable == null)
			return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}
}