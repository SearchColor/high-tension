package com.high.payment.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.high.payment.domain.model.Payment;
import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.model.PaymentStatus;
import com.high.payment.domain.port.out.PaymentRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

	private final PaymentJpaRepository jpaRepository;

	@Override
	public Payment save(Payment payment) {
		return jpaRepository.save(payment);
	}

	@Override
	public Optional<Payment> findById(UUID id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Optional<Payment> findByOrderId(UUID orderId) {
		return jpaRepository.findByOrderId(orderId);
	}

	@Override
	public List<Payment> findAllByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime before, int limit) {
		return jpaRepository.findByStatusAndCreatedAtBefore(status, before, PageRequest.of(0, limit));
	}
}
