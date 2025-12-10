package com.high.product.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.library.jpa.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_processed_saga")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProcessedSaga extends BaseEntity {

	@Id
	@Column(name = "saga_id", columnDefinition = "BINARY(16)")
	private UUID sagaId;

	@Column(name = "order_id", columnDefinition = "BINARY(16)")
	private UUID orderId;

	private String sagaType;

	private String status;

	private LocalDateTime processedAt;

	public ProcessedSaga(UUID sagaId, UUID orderId, String sagaType, String status) {
		this.sagaId = sagaId;
		this.orderId = orderId;
		this.sagaType = sagaType;
		this.status = status;
		this.processedAt = LocalDateTime.now();
	}
}
