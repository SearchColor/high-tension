package com.high.payment.infrastrcutre.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.dto.PaymentSagaEventPort;
import com.high.payment.application.dto.PaymentSagaResultMessage;
import com.high.payment.infrastrcutre.kafka.dto.PaymentCreateFailMessage;
import com.high.payment.infrastrcutre.kafka.dto.PaymentCreateSuccessMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisherImpl implements PaymentSagaEventPort {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	private static final String TOPIC_SUCCESS = "payment-create-success";
	private static final String TOPIC_FAIL = "payment-create-fail";


	@Override
	public void publishPaymentResult(PaymentSagaResultMessage result) {
		if (result.success()) {
			// 성공: 내부 DTO -> 외부 Kafka DTO 변환
			PaymentCreateSuccessMessage successMessage = new PaymentCreateSuccessMessage(
				result.sagaId(),
				result.orderId(),
				result.userId()
			);

			try {
				String json = objectMapper.writeValueAsString(successMessage);
				kafkaTemplate.send(TOPIC_SUCCESS, String.valueOf(successMessage.sagaId()), json);
				log.info("[Publisher] 결제 성공 이벤트 발행 완료: SagaId={}, orderId={}, userId={}", successMessage.sagaId(), successMessage.orderId(), successMessage.userId());
			} catch (JsonProcessingException e) {
				log.error("[Publisher] 성공 메시지 직렬화 실패", e);
				throw new RuntimeException(e);
			}

		} else {
			// 실패: 내부 DTO -> 외부 Kafka DTO 변환
			PaymentCreateFailMessage failMessage = new PaymentCreateFailMessage(
				result.sagaId(),
				result.orderId(),
				result.message(),
				result.userId()
			);

			try {
				String json = objectMapper.writeValueAsString(failMessage);
				kafkaTemplate.send(TOPIC_FAIL, failMessage.sagaId().toString(), json);
				log.info("[Publisher] 결제 실패 이벤트 발행 완료: sagaId={}, orderId={}, userId={}, message={}",
						 failMessage.sagaId(), failMessage.orderId(), failMessage.userId(), failMessage.message());
			} catch (JsonProcessingException e) {
				log.error("[Publisher] 결제 실패 메시지 직렬화 실패", e);
				throw new RuntimeException(e);
			}
		}
	}
}
