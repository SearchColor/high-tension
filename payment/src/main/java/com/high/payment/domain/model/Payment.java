package com.high.payment.domain.model;

import java.math.BigDecimal;
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
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "order_id", nullable = false, unique = true)
	private UUID orderId; // 주문 서비스에서 넘어온 ID

	@Column(name = "user_id", nullable = false, length = 50)
	private String userId;

	@Column(name = "payment_amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod; // 예: CARD, TRANSFER

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false)
	private PaymentStatus status;

	@Column(name = "pg_tid")
	private String pgTid; // PG사 거래 ID

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	public void fail() {
		this.status = PaymentStatus.FAILED;
	}

	public void complete(String pgTid) {
		this.status = PaymentStatus.COMPLETED;
		this.pgTid = pgTid;
	}

	public void cancel() {
		if (this.status != PaymentStatus.COMPLETED) {
			throw new IllegalStateException("COMPLETED 상태가 아니면 취소할 수 없습니다.");
		}
		this.status = PaymentStatus.CANCELED;
	}
}
