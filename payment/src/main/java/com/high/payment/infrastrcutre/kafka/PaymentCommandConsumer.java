package com.high.payment.infrastrcutre.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.payment.application.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandConsumer {
	private final PaymentService paymentService;
	private final KafkaTemplate<String, Object> kafkaTemplate; // Object로 정의하면 DTO 전송 가능

	@KafkaListener(topics = "payment-create-request", groupId = "payment-service-saga-group")
	public void handlePaymentCommand(PaymentCreateCommandRequest request) {
		log.info("[SAGA] 결제 Command 수신. SagaId={}, OrderId={}",
				 request.sagaId(), request.orderId());

		try {
			// 1. 핵심 비즈니스 로직 호출 (유저 검증, 결제 DB 저장 등)
			paymentService.processPaymentSaga(request);

			// 2. 결제 성공 이벤트 발행
			PaymentCreateSuccessMessage successMessage = PaymentCreateSuccessMessage.from(request);
			kafkaTemplate.send("payment-create-success", successMessage);
			log.info("[SAGA] 결제 성공 이벤트 발행 완료. SagaId: {}", request.sagaId());

		} catch (Exception e) {
			log.error("[SAGA] 결제 처리 실패. SagaId: {}", request.sagaId(), e);
		}
	}
}
