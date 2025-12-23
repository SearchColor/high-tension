package com.high.product.infrastructure.kafka.producer;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.domain.model.KafkaOutbox;
import com.high.product.domain.repository.KafkaOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOutboxPublisher {

	private final KafkaOutboxRepository kafkaOutboxRepository;
	private final ProductKafkaPublisher kafkaPublisher;

	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void publishPendingMessages() {
		List<KafkaOutbox> pending = kafkaOutboxRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING");
		for (KafkaOutbox outbox : pending) {
			try {
				kafkaPublisher.send(outbox.getTopic(), outbox.getMessageKey(), outbox.getPayload());
				kafkaOutboxRepository.markAsSent(outbox);
				log.info("KafkaOutbox 발행 완료: {}", outbox.getId());
			} catch (Exception e) {
				outbox.increaseRetry();
				if (outbox.getRetryCount() >= 3) {
					outbox.markFailed();
				}
				kafkaOutboxRepository.save(outbox);
				log.error("KafkaOutbox 발행 실패: {}", outbox.getId(), e);
			}
		}
	}
}
