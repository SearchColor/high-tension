package com.high.payment.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import com.high.payment.domain.model.PaymentOutbox.OutboxStatus;
import com.high.payment.domain.model.PaymentOutbox;

public interface PaymentOutboxRepositoryPort {
	PaymentOutbox save(PaymentOutbox outbox);

	Optional<PaymentOutbox> findById(UUID id);

	// 발행되지 않은 이벤트 목록을 조회합니다.
	List<PaymentOutbox> findTop100ByOrderByCreatedAtAsc();

	List<PaymentOutbox> findAllByStatus(OutboxStatus status, Pageable pageable);

	//  발행된 이벤트들을 삭제합니다.
	void deleteAll(List<PaymentOutbox> outboxes);
}
