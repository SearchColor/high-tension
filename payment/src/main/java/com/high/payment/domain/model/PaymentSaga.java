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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_saga")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSaga {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private UUID sagaId;

	@Column(nullable = false)
	private UUID orderId;

	@Column(nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentSagaStatus status;

	@Column
	private Integer errorCode;

	@Column(length = 500)
	private String message;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public static PaymentSaga start(UUID sagaId, UUID orderId, UUID userId) {
		LocalDateTime now = LocalDateTime.now();
		return PaymentSaga.builder()
						  .sagaId(sagaId)
						  .orderId(orderId)
						  .userId(userId)
						  .status(PaymentSagaStatus.PENDING)
						  .createdAt(now)
						  .updatedAt(now)
						  .build();
	}

	public void success(String message) {
		this.status = PaymentSagaStatus.SUCCESS;
		this.message = message;
		this.updatedAt = LocalDateTime.now();
	}

	public void fail(Integer errorCode, String message) {
		this.status = PaymentSagaStatus.FAIL;
		this.errorCode = errorCode;
		this.message = message;
		this.updatedAt = LocalDateTime.now();
	}

	public void timeout(String message) {
		this.status = PaymentSagaStatus.TIMEOUT;
		this.message = message;
		this.updatedAt = LocalDateTime.now();
	}
}
