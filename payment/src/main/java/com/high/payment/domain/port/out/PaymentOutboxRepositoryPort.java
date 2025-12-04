package com.high.payment.domain.port.out;

import com.high.payment.domain.model.PaymentOutbox;

public interface PaymentOutboxRepositoryPort {
	PaymentOutbox save(PaymentOutbox outbox);
}
