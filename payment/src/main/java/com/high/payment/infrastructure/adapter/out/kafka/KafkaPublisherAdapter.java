package com.high.payment.infrastructure.adapter.out.kafka;

import org.springframework.stereotype.Component;

import com.high.payment.application.port.out.KafkaMessagePublisherPort;
import com.high.payment.domain.model.PaymentOutbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPublisherAdapter implements KafkaMessagePublisherPort {

	@Override
	public void publish(PaymentOutbox outbox, String topic) {
		log.info("[Kafka Mock] 이벤트 발행 시뮬레이션 시작.");

		// 1. 발행 정보 로깅
		log.info("-> Topic: {}, EventType: {}, Outbox ID: {}",
				 topic, outbox.getEventType(), outbox.getId());

		// 2. 실제 페이로드 로깅 (디버그 레벨)
		log.debug("-> Payload: {}", outbox.getPayload());

		// 3. (Mock 성공 가정)
		log.info("[Kafka Mock] 이벤트 발행 시뮬레이션 성공적으로 완료.");
	}
}
