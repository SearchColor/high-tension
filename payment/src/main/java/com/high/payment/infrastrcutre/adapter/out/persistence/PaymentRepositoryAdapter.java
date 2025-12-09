package com.high.payment.infrastrcutre.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.high.payment.domain.model.Payment;
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
}
