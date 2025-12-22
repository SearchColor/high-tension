package com.high.product.domain.repository;

import java.util.List;

import com.high.product.domain.model.KafkaOutbox;

public interface KafkaOutboxRepository {

	List<KafkaOutbox> findTop100ByStatusOrderByCreatedAtAsc(String status);

	KafkaOutbox save(KafkaOutbox outbox);

	void markAsSent(KafkaOutbox outbox);
}
