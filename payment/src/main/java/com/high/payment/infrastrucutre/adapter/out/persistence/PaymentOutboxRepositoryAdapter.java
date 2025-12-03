package com.high.payment.infrastrucutre.adapter.out.persistence;

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
}
