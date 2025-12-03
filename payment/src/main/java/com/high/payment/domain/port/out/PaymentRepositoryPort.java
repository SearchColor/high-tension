package com.high.payment.domain.port.out;

import com.high.payment.domain.model.Payment;

public interface PaymentRepositoryPort {
	Payment save(Payment payment);
	// Payment findById(UUID id); // 필요 시 추가
}
