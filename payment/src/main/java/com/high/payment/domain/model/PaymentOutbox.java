package com.high.payment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOutbox {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID aggregateId; // Payment ID
	private String aggregateType; // "PAYMENT"
	private String eventType;     // "payment.completed" or "payment.failed"

	@Column(columnDefinition = "TEXT") // JSON Payload가 길 수 있음
	private String payload;

	private String status; // PENDING, PUBLISHED

	private LocalDateTime createdAt;

	public static PaymentOutbox create(UUID aggregateId, String eventType, String payload) {
		PaymentOutbox outbox = new PaymentOutbox();
		outbox.aggregateId = aggregateId;
		outbox.aggregateType = "PAYMENT";
		outbox.eventType = eventType;
		outbox.payload = payload;
		outbox.status = "PENDING";
		outbox.createdAt = LocalDateTime.now();
		return outbox;
	}

}
