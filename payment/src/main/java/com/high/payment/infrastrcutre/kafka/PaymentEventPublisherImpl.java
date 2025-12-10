package com.high.payment.infrastrcutre.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateFailMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.payment.application.dto.PaymentEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	private static final String TOPIC_SUCCESS = "payment-create-success";
	private static final String TOPIC_FAIL = "payment-create-fail";

	@Override
	public void publishSuccess(PaymentCreateSuccessMessage message) {
		try {
			String json = objectMapper.writeValueAsString(message);
			// 메시지 키로 SagaId를 사용하여 순서 보장 (선택사항)
			kafkaTemplate.send(TOPIC_SUCCESS, String.valueOf(message.sagaId()), json);
			log.info("[Publisher] 결제 성공 이벤트 발행 완료: SagaId={}", message.sagaId());
		} catch (JsonProcessingException e) {
			log.error("[Publisher] 성공 메시지 직렬화 실패", e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public void publishFail(PaymentCreateFailMessage message) {
		try {
			String json = objectMapper.writeValueAsString(message);
			kafkaTemplate.send(TOPIC_FAIL, String.valueOf(message.sagaId()), json);
			log.info("[Publisher] 결제 실패 이벤트 발행 완료: SagaId={}, Reason={}", message.sagaId(), message.reason());
		} catch (JsonProcessingException e) {
			log.error("[Publisher] 실패 메시지 직렬화 실패", e);
			throw new RuntimeException(e);
		}
	}
}
