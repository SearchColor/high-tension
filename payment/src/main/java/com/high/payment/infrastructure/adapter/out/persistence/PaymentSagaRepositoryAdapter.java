package com.high.payment.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.high.payment.domain.model.PaymentSaga;
import com.high.payment.domain.port.out.PaymentSagaRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentSagaRepositoryAdapter implements PaymentSagaRepositoryPort {

	private final JpaPaymentSagaRepository jpaRepo;

	@Override
	public PaymentSaga save(PaymentSaga saga) {
		return jpaRepo.save(saga);
	}

	@Override
	public Optional<PaymentSaga> findBySagaId(UUID sagaId) {
		return jpaRepo.findBySagaId(sagaId);
	}
}
