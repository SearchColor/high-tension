package com.high.product.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.high.product.domain.model.KafkaOutbox;
import com.high.product.domain.repository.KafkaOutboxRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaOutboxAdapter implements KafkaOutboxRepository {

	private final JpaKafkaOutboxRepository jpaKafkaOutboxRepository;

	@Override
	public List<KafkaOutbox> findTop100ByStatusOrderByCreatedAtAsc(String status) {
		return jpaKafkaOutboxRepository.findTop100ByStatusOrderByCreatedAtAsc(status);
	}

	@Override
	public KafkaOutbox save(KafkaOutbox outbox) {
		return jpaKafkaOutboxRepository.save(outbox);
	}

	@Override
	public void markAsSent(KafkaOutbox outbox) {
		// 1. 엔티티 내부의 상태 변경 메서드 호출 (비즈니스 로직)
		outbox.markSent();
		// 2. 변경된 엔티티를 DB에 저장
		jpaKafkaOutboxRepository.save(outbox);
	}
}
