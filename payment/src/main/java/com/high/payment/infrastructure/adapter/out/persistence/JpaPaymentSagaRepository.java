package com.high.payment.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.high.payment.domain.model.PaymentSaga;

public interface JpaPaymentSagaRepository extends JpaRepository<PaymentSaga, UUID> {
	Optional<PaymentSaga> findBySagaId(UUID sagaId);
}
