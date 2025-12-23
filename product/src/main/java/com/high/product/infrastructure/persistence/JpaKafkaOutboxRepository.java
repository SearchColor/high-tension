package com.high.product.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.high.product.domain.model.KafkaOutbox;

@Repository
public interface JpaKafkaOutboxRepository extends JpaRepository<KafkaOutbox, UUID> {

	List<KafkaOutbox> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
