package com.high.orchestration.infrastructure.kafka.producer;

import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponRestoreCommandRequest;
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
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaMessageSender sender;


    @Override
    public void publishOrderCreateCommand(String topic, OrderCreateCommandRequest orderCreateCommandRequest) {
        sender.send(topic, orderCreateCommandRequest);
        log.info("[KafkaEventPublisher] publicOrderCreateCommand 이벤트 발행 성공");
    }

    @Override
    public void publishStockDeductionCommand(String topic, StockDeductionCommandRequest stockDeductionCommandRequest) {
        sender.send(topic, stockDeductionCommandRequest);
        log.info("[KafkaEventPublisher] publicStockDeductionCommand 이벤트 발행 성공");

    }

    @Override
    public void publishPaymentCreateCommand(String topic, PaymentCreateCommandRequest paymentCreateCommandRequest) {
        sender.send(topic, paymentCreateCommandRequest);
        log.info("[KafkaEventPublisher] publicPaymentCreateCommand 이벤트 발행 성공");
    }

    @Override
    public void publishClearCartCommand(String topic, ClearCartCommandRequest clearCartCommandRequest) {
        sender.send(topic, clearCartCommandRequest);
        log.info("[KafkaEventPublisher] publicClearCartCommand 이벤트 발행 성공");
    }

    @Override
    public void publishOrderSuccessProcessingCommand(String topic, ProcessOrderSuccessCommandRequest processOrderSuccessCommandRequest) {
        sender.send(topic, processOrderSuccessCommandRequest);
        log.info("[KafkaEventPublisher] publishOrderSuccessProcessingCommand 이벤트 발행 성공");

    }

    @Override
    public void publishStockRestoreCommand(String topic, StockRestoreCommandRequest stockRestoreCommandRequest) {
        sender.send(topic, stockRestoreCommandRequest);
        log.info("[KafkaEventPublisher] publishStockRestoreCommand 이벤트 발행 성공");

    }

    @Override
    public void publishCouponUseCommand(String topic, CouponUseCommandRequest couponUseCommandRequest) {
        sender.send(topic, couponUseCommandRequest);
        log.info("[KafkaEventPublisher] publicCouponUserCommand 이벤트 발행 성공");
    }

    @Override
    public void publishOrderDeleteCommand(String topic, OrderDeleteCommandRequest orderDeleteCommandRequest) {
        sender.send(topic, orderDeleteCommandRequest);
        log.info("[KafkaEventPublisher] publicOrderDeleteCommand 보상트랜잭션 주문 삭제 이벤트 발행 성공");
    }

    @Override
    public void publishCouponRestoreCommand(String topic, CouponRestoreCommandRequest couponRequest) {
        sender.send(topic, couponRequest);
        log.info("[KafkaEventPublisher] publishCouponRestoreCommand 보상트랜잭션 쿠폰 복원 이벤트 발행 성공");

    }



}
