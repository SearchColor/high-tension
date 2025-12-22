package com.high.payment.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentOutboxRepositoryAdapter implements PaymentOutboxRepositoryPort {

	private final PaymentOutboxJpaRepository jpaRepository;

	@Override
	public PaymentOutbox save(PaymentOutbox outbox) {
		return jpaRepository.save(outbox);
	}

	@Override
	public Optional<PaymentOutbox> findById(UUID id) {
		return jpaRepository.findById(id);
	}

	@Override
	public List<PaymentOutbox> findTop100ByOrderByCreatedAtAsc() {
		return jpaRepository.findTop100ByOrderByCreatedAtAsc();
	}

	@Override
	public List<PaymentOutbox> findAllByStatus(PaymentOutbox.OutboxStatus status, Pageable pageable) {
		return jpaRepository.findAllByStatus(status, pageable);
	}

	@Override
	public void deleteAll(List<PaymentOutbox> outboxes) {
		jpaRepository.deleteAll(outboxes);
	}


	@Override
	public List<PaymentOutbox> findPending(int batchSize) {
		Pageable pageable = PageRequest.of(
			0,
			batchSize,
			Sort.by(Sort.Direction.ASC, "createdAt")
		);

		return jpaRepository.findAllByStatus(PaymentOutbox.OutboxStatus.PENDING, pageable);
	}
}
