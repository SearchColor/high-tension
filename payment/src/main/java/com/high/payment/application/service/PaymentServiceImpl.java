package com.high.payment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.PaymentCompletedEvent;
import com.high.payment.application.port.out.IamportClientPort;
import com.high.payment.domain.model.Payment;
import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.model.PaymentStatus;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;
import com.high.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentOutboxRepositoryPort outboxRepository;
	private final IamportClientPort iamportClient;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional // DB 저장과 Outbox 저장을 하나의 트랜잭션으로 묶음
	public void processPayment(CreatePaymentRequest event) {
		log.info("결제 프로세스 시작: OrderId={}", event.orderId());

		String pgTid = null;
		String eventType = "payment.completed";
		PaymentStatus initStatus = PaymentStatus.REQUESTED;

		// 1. 도메인 객체 생성 (아직 저장 안 함)
		Payment payment = Payment.builder()
								 .orderId(event.orderId())
								 .userId(event.userId())
								 .amount(event.amount())
								 .paymentMethod(event.paymentMethod())
								 .status(initStatus)
								 .build();

		try {
			// 2. PG사 결제 승인 요청 (외부 연동)
			pgTid = iamportClient.requestPayment(event.orderId(), event.amount(), event.paymentMethod());

			// 3. 성공 시 상태 변경
			payment.complete(pgTid);
			log.info("PG 결제 승인 성공: TID={}", pgTid);

		} catch (Exception e) {
			// 4. 실패 시 상태 변경
			payment.fail();
			eventType = "payment.failed";
			log.error("PG 결제 승인 실패: {}", e.getMessage());
		}

		// 5. Payment 저장
		Payment savedPayment = paymentRepository.save(payment);

		// 6. Outbox 저장 (이벤트 발행 보장)
		try {
			PaymentCompletedEvent outboxPayload = new PaymentCompletedEvent(
				savedPayment.getId(),
				savedPayment.getOrderId(),
				savedPayment.getUserId(),
				savedPayment.getAmount(),
				savedPayment.getStatus().name()
			);

			String jsonPayload = objectMapper.writeValueAsString(outboxPayload);
			PaymentOutbox outbox = PaymentOutbox.create(savedPayment.getId(), eventType, jsonPayload);

			outboxRepository.save(outbox);

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Outbox JSON 변환 에러", e);
		}

	}
}
