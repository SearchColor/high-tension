package com.high.payment.infrastrcutre.adapter.out.persistence;

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
}
