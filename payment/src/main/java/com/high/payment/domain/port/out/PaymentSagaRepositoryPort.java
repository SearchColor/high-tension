package com.high.payment.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.high.payment.domain.model.PaymentSaga;

public interface PaymentSagaRepositoryPort {
	PaymentSaga save(PaymentSaga saga);
	Optional<PaymentSaga> findBySagaId(UUID sagaId);
}
