package com.high.orchestration.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.port.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public void publishOrderCreateCommand(String topic, OrderCreateCommandRequest orderCreateCommandRequest) {
        send(topic, orderCreateCommandRequest);
        log.info("[KafkaEventPublisher] publicOrderCreateCommand 이벤트 발행 성공");
    }

    @Override
    public void publishStockDeductionCommand(String topic, StockDeductionCommandRequest stockDeductionCommandRequest) {
        send(topic, stockDeductionCommandRequest);
        log.info("[KafkaEventPublisher] publicStockDeductionCommand 이벤트 발행 성공");

    }

    @Override
    public void publishPaymentCreateCommand(String topic, PaymentCreateCommandRequest paymentCreateCommandRequest) {
        send(topic, paymentCreateCommandRequest);
        log.info("[KafkaEventPublisher] publicPaymentCreateCommand 이벤트 발행 성공");
    }

    @Override
    public void publishClearCartCommand(String topic, ClearCartCommandRequest clearCartCommandRequest) {
        send(topic, clearCartCommandRequest);
    }


    private void send(String topic, Object messageObj) {
        String json = toJson(messageObj);
        kafkaTemplate.send(topic, json);
        log.info("[KafkaEventPublisher] topic={}, message={}", topic, json);
    }

    private String toJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            if (json == null || json.isEmpty()) {
                throw new RuntimeException("[KafkaEventPublisher] Kafka 메시지 직렬화 실패: 빈 데이터");
            }
            return json;
        } catch (Exception e) {
            log.error("[KafkaEventPublisher] Kafka 메시지 JSON 변환 실패: {}", obj, e);
            throw new RuntimeException("Kafka 메시지 직렬화 오류", e);
        }
    }
}
