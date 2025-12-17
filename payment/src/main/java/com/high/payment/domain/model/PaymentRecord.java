package com.high.payment.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_payment_record")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "record_id")
	private UUID recordId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(name = "payment_memo")
	private String paymentMemo;

	public static PaymentRecord of(Payment payment, String memo) {
		return PaymentRecord.builder()
							.payment(payment)
							.paymentMemo(memo)
							.build();
	}
}
