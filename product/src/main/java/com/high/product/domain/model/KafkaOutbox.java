package com.high.product.domain.model;

import java.util.UUID;

import com.library.jpa.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_kafka_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KafkaOutbox extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String topic;
	private String messageKey;

	@Lob
	private String payload;

	private String status; // PENDING, SENT, FAILED
	private int retryCount;

	private UUID sagaId;
	private UUID orderId;
	private UUID userId;

	public void markSent() {
		this.status = "SENT";
	}

	public void markFailed() {
		this.status = "FAILED";
	}

	public void increaseRetry() {
		this.retryCount++;
	}

	public KafkaOutbox(String topic, String payload, String status, UUID sagaId, UUID orderId, UUID userId) {
		this.topic = topic;
		this.payload = payload;
		this.status = status;
		this.sagaId = sagaId;
		this.orderId = orderId;
		this.userId = userId;
	}
}
