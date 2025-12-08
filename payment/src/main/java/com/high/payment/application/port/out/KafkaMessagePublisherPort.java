package com.high.payment.application.port.out;

import com.high.payment.domain.model.PaymentOutbox;

public interface KafkaMessagePublisherPort {
	// Outbox 이벤트를 받아 지정된 토픽에 발행합니다.
	void publish(PaymentOutbox outbox, String topic);
}
