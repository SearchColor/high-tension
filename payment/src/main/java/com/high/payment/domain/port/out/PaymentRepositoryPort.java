package com.high.payment.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.high.payment.domain.model.Payment;
import com.high.payment.domain.model.PaymentStatus;

public interface PaymentRepositoryPort {
	Payment save(Payment payment);

	Optional<Payment> findById(UUID id);
	
	Optional<Payment> findByOrderId(UUID orderId);

	List<Payment> findAllByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime before, int limit);
}
