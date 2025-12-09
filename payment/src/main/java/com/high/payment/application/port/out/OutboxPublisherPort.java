package com.high.payment.application.port.out;

import java.util.List;

import com.high.payment.domain.model.PaymentOutbox;

public interface OutboxPublisherPort {
	List<PaymentOutbox> findPendingEvents(int limit);

	void publish(PaymentOutbox event);

	void markAsSent(PaymentOutbox event);
}
