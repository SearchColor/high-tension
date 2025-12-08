package com.high.payment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentOutbox {

	public enum OutboxStatus {
		PENDING, SENT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID aggregateId; // Payment ID
	private String aggregateType; // "PAYMENT"
	@Column(name = "event_type", nullable = false)
	private String eventType;     // "payment.completed" or "payment.failed"

	@Column(columnDefinition = "TEXT") // JSON Payload가 길 수 있음
	private String payload;

	private String topic;
	private String messageKey;

	@Enumerated(EnumType.STRING)
	private OutboxStatus status; // PENDING, PUBLISHED

	private LocalDateTime createdAt;

	public static PaymentOutbox create(UUID aggregateId, String eventType, String payload) {
		return PaymentOutbox.builder()
							.aggregateId(aggregateId)
							.aggregateType("PAYMENT")
							.eventType(eventType)
							.payload(payload)
							.topic("payment-events") // 기본 토픽 설정 (필요시 파라미터로 받기)
							.messageKey(aggregateId.toString()) // 기본 키 설정
							.status(OutboxStatus.PENDING) // Enum 사용
							.createdAt(LocalDateTime.now())
							.build();
	}

	public void markAsSent() {
		this.status = OutboxStatus.SENT;
	}

	public String getTopic(){
		return this.eventType;
	}
}
