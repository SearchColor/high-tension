package com.high.payment.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;
import com.high.payment.infrastructure.kafka.PaymentDomainEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentOutboxRelayScheduler {

	private final PaymentOutboxRepositoryPort outboxRepository;
	private final PaymentDomainEventPublisher publisher;

	@Value("${payment.outbox.relay.batch-size:200}")
	private int batchSize;

	@Scheduled(fixedDelayString = "${payment.outbox.relay.fixed-delay-ms:1000}")
	@Transactional
	public void relay() {
		// PENDING outbox를 batchSize만큼 조회
		List<PaymentOutbox> pending = outboxRepository.findPending(batchSize);

		if (pending.isEmpty()) return;

		for (PaymentOutbox outbox : pending) {
			try {
				publisher.publish(outbox);     // Kafka 발행
				outbox.markAsSent();// SENT로 변경
				outboxRepository.save(outbox);

			} catch (Exception e) {
				log.error("[OutboxRelay] publish failed. outboxId={}, eventType={}",
						  outbox.getId(), outbox.getEventType(), e);
				// 실패는 재시도 대상이므로 status 변경하지 않음
			}
		}
	}
}
