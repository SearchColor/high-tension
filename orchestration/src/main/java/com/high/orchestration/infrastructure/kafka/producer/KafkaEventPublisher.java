package com.high.orchestration.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponUseCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderDeleteCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.ProcessOrderSuccessCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockRestoreCommandRequest;
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

    @Override
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
        log.info("[KafkaEventPublisher] publicClearCartCommand 이벤트 발행 성공");
    }

    @Override
    public void publishOrderSuccessProcessingCommand(String topic, ProcessOrderSuccessCommandRequest processOrderSuccessCommandRequest) {
        send(topic, processOrderSuccessCommandRequest);
        log.info("[KafkaEventPublisher] publishOrderSuccessProcessingCommand 이벤트 발행 성공");

    }

    @Override
    public void publishStockRestoreCommand(String topic, StockRestoreCommandRequest stockRestoreCommandRequest) {
        send(topic, stockRestoreCommandRequest);
        log.info("[KafkaEventPublisher] publishStockRestoreCommand 이벤트 발행 성공");

    }

    @Override
    public void publishCouponUseCommand(String topic, CouponUseCommandRequest couponUseCommandRequest) {
        send(topic, couponUseCommandRequest);
        log.info("[KafkaEventPublisher] publicCouponUserCommand 이벤트 발행 성공");
    }

    @Override
    public void publishOrderDeleteCommand(String topic, OrderDeleteCommandRequest orderDeleteCommandRequest) {
        send(topic, orderDeleteCommandRequest);
        log.info("[KafkaEventPublisher] publicOrderDeleteCommand 보상트랜잭션 이벤트 발행 성공");
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
