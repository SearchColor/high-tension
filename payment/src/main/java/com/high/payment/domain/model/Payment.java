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
@Table(name = "p_payments")
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
	private UUID userId;

	@Column(name = "payment_price", nullable = false)
	private Integer paymentPrice;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod; // 예: CARD, TRANSFER

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false)
	private PaymentStatus status;

	@Column(name = "pg_tid")
	private String pgTid; // PG사 거래 ID

	@Column(name= "payment_time")
	private LocalDateTime paymentTime;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	public void fail() {
		this.status = PaymentStatus.FAILED;
	}

	public void complete(String pgTid) {
		this.status = PaymentStatus.COMPLETED;
		this.pgTid = pgTid;
		this.paymentTime = LocalDateTime.now();
	}

	public void cancel() {
		if (this.status == PaymentStatus.CANCELED)
			return;

		if (this.status == PaymentStatus.FAILED) {
			throw new IllegalStateException("FAILED 상태는 취소할 수 없습니다.");
		}
		this.status = PaymentStatus.CANCELED;
		this.paymentTime = LocalDateTime.now();
	}
}
