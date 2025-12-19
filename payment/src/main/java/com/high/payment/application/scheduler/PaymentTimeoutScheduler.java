package com.high.payment.application.scheduler;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.dto.PaymentCanceledEvent;
import com.high.payment.domain.model.Payment;
import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.model.PaymentStatus;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;
import com.high.payment.domain.port.out.PaymentRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeoutScheduler {

	private final PaymentRepositoryPort paymentRepositoryPort;
	private final PaymentOutboxRepositoryPort outboxRepository;
	private final ObjectMapper objectMapper;

	@Value("${payment.intent.ttl-minutes:15}")
	private long ttlMinutes;

	@Value("${payment.scheduler.payment-timeout.batch-size:200}")
	private int batchSize;

	@Scheduled(fixedDelayString = "${payment.scheduler.payment-timeout.fixed-delay-ms:60000}")
	@Transactional
	public void cancelExpiredPayments() {
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(ttlMinutes);

		var expired = paymentRepositoryPort.findAllByStatusAndCreatedAtBefore(
			PaymentStatus.PENDING,
			threshold,
			batchSize
		);

		if (expired.isEmpty()) return;

		log.info("[Timeout] 만료 결제 발견: count={}, threshold={}", expired.size(), threshold);

		for (Payment payment : expired) {
			// 혹시 중복 처리 방지 (상태가 이미 바뀌었으면 스킵)
			if (payment.getStatus() != PaymentStatus.PENDING) continue;

			// 상태 변경
			payment.cancel();
			paymentRepositoryPort.save(payment);

			// Outbox 이벤트 저장
			try {
				PaymentCanceledEvent payload = new PaymentCanceledEvent(
					payment.getId(),
					payment.getOrderId(),
					payment.getUserId(),
					payment.getPaymentPrice(),
					"PAYMENT_TIMEOUT",
					LocalDateTime.now()
				);

				String json = objectMapper.writeValueAsString(payload);

				PaymentOutbox outbox = PaymentOutbox.create(
					payment.getId(),
					"payment.canceled",
					json
				);

				outboxRepository.save(outbox);

			} catch (JsonProcessingException e) {
				log.error("[Timeout] Outbox payload 직렬화 실패 paymentId={}", payment.getId(), e);
			}
		}
	}
}
