package com.high.payment.application.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.payment.application.port.out.KafkaMessagePublisherPort;
import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPollingPublisherService {

	private final PaymentOutboxRepositoryPort outboxRepository;
	private final KafkaMessagePublisherPort kafkaPublisher;

	private static final String PAYMENT_TOPIC = "payment-events";

	@Scheduled(fixedRate = 5000) // 5초마다 실행
	@Transactional // 조회 및 삭제를 하나의 트랜잭션으로 묶어 원자성을 보장
	public void publishOutboxEvents() {
		// 1. 미발행된 이벤트 조회 (최대 100건)
		List<PaymentOutbox> pendingEvents = outboxRepository.findTop100ByOrderByCreatedAtAsc();

		if (pendingEvents.isEmpty()) {
			return;
		}

		log.info("[Scheduler] {}건의 Outbox 이벤트를 발견했습니다. 발행을 시작합니다.", pendingEvents.size());

		// 2. 이벤트 발행
		for (PaymentOutbox outbox : pendingEvents) {
			try {
				// Outbox 데이터와 정의된 토픽으로 Kafka 발행 Port 호출
				kafkaPublisher.publish(outbox, PAYMENT_TOPIC);
				log.debug("Outbox ID: {} 발행 성공.", outbox.getId());
			} catch (Exception e) {
				log.error(" Outbox ID: {} Kafka 발행 실패. 재시도 예정.", outbox.getId(), e);

			}
		}

		// 트랜잭션이 성공적으로 커밋될 경우에만 삭제가 확정됩니다.
		outboxRepository.deleteAll(pendingEvents);
		log.info("{}건의 Outbox 이벤트 발행 및 DB 삭제 처리 완료.", pendingEvents.size());
	}
}
