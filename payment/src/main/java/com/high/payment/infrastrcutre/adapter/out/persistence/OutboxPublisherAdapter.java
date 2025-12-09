package com.high.payment.infrastrcutre.adapter.out.persistence;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.high.payment.application.port.out.OutboxPublisherPort;
import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;
import com.high.payment.infrastrcutre.adapter.out.client.KafkaProducerPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherAdapter implements OutboxPublisherPort {

	private final PaymentOutboxRepositoryPort outboxRepository;
	private final KafkaProducerPort kafkaProducerPort;

	@Override
	public List<PaymentOutbox> findPendingEvents(int limit) {
		return outboxRepository.findAllByStatus(PaymentOutbox.OutboxStatus.PENDING, PageRequest.of(0, limit));
	}

	@Override
	public void publish(PaymentOutbox event) {
		kafkaProducerPort.send(event.getTopic(), event.getMessageKey(), event.getPayload());
		log.info("[KAFKA] Outbox Event ID {} 발행 완료.", event.getId());
	}

	@Override
	public void markAsSent(PaymentOutbox event) {

	}
}
