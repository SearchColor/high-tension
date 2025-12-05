package com.high.payment.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.high.payment.domain.model.Payment;

public interface PaymentRepositoryPort {
	Payment save(Payment payment);

	Optional<Payment> findById(UUID id);
}
