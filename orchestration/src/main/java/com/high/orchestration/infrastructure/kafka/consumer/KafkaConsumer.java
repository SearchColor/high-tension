package com.high.orchestration.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.OrderCreateSagaService;
import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponRestoreCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponUseCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderDeleteCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.ProcessOrderSuccessCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockRestoreCommandRequest;
import com.high.orchestration.application.dto.internal.response.OrderCreateFailCommandResponse;
import com.high.orchestration.infrastructure.adaptor.OrderCreateAdapter;
import com.high.orchestration.infrastructure.exception.FailToConvertMessageException;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateFailedMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateFailMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.StockDeductionFailMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.StockDeductionSuccessMessage;
import com.high.orchestration.infrastructure.kafka.producer.KafkaEventPublisher;
import com.high.orchestration.monitoring.application.DlqRetryFailureHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

        private final OrderCreateSagaService orderCreateSagaService;
        private final ObjectMapper objectMapper;
        private final OrderCreateAdapter adapter;
        private final KafkaEventPublisher publisher;
        private final DlqRetryFailureHandler dlqRetryFailureHandler;
        
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(
            delay = 1000,
            multiplier = 2.0 //지수 적용
        ),
        dltTopicSuffix = "-dlq"
    )
    @KafkaListener(topics = "order-create-success", groupId = "orchestration-consumer-group")
    public void orderCreateSuccess(String orderCreateSuccessMessage,
        @Header(name = "from-dlq", required = false) Boolean fromDlq,
        @Header(name = "outboxId", required = false) String outboxId
    ) throws JsonProcessingException {
        OrderCreateSuccessMessage message = null;
        log.info(
            "[KafkaConsumer] orderCreateSuccess - fromDlq={}, payload={}",
            fromDlq, orderCreateSuccessMessage
        );

        try {

            message = objectMapper.readValue(orderCreateSuccessMessage,
                OrderCreateSuccessMessage.class);

        StockDeductionCommandRequest stockRequest = null;
        CouponUseCommandRequest couponRequest = null;


            stockRequest = adapter.toStockDeductionCommand(message);
            couponRequest = adapter.toCouponUseCommandRequest(message);

            orderCreateSagaService.handlerOrderCreateSuccess(stockRequest);

            publisher.publishCouponUseCommand("coupon-use-request", couponRequest);
            publisher.publishStockDeductionCommand("stock-deduction-request", stockRequest);
            log.info("[KafkaConsumer] orderCreateSuccess : 재고차감 명령 발행 성공 ");

        }

        catch (Exception e) {
            log.error("[KafkaConsumer] orderCreateSuccess 실패", e);

            //DLQ에서 온 메시지가 실패하면 즉시 영구 실패 처리
            if (Boolean.TRUE.equals(fromDlq) && outboxId != null) {
                log.error(
                    "[KafkaConsumer] DLQ에서 유입된 재시도 메시지 실패 - 영구 실패 처리 - Id={}",
                    outboxId
                );
                dlqRetryFailureHandler.handleRetryFailure(outboxId, e);
                return;
            }

            throw e;
        }
    }


    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = "-dlq"
    )
    @KafkaListener(topics = "order-create-fail", groupId = "orchestration-consumer-group")
    public void orderCreateFail(String orderCreateFailMessage) throws JsonProcessingException {
        log.info("[kafkaConsumer] orderCreateFail : orderCreateFailMessage {}", orderCreateFailMessage);

            OrderCreateFailedMessage message = objectMapper.readValue(orderCreateFailMessage, OrderCreateFailedMessage.class);
            OrderCreateFailCommandResponse request = adapter.toOrderCreateFailCommand(message);
            orderCreateSagaService.handleOrderCreateFailed(request);

            log.info("주문 생성 실패 메시지 처리 완료");

    }

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = "-dlq"
    )
    @KafkaListener(topics = "stock-deduction-success", groupId = "orchestration-consumer-group")
    public void stockDeductionSuccess(String stockDeductionSuccessMessage) throws JsonProcessingException {
        log.info("[KafkaConsumer] stockDeductionSuccess :  stockDeductionSuccessMessage: {}", stockDeductionSuccessMessage);

            StockDeductionSuccessMessage message = objectMapper.readValue(stockDeductionSuccessMessage, StockDeductionSuccessMessage.class);
            PaymentCreateCommandRequest request = adapter.toPaymentCreateCommand(message);
            orderCreateSagaService.handlerStockDeductionSuccess(request);

            publisher.publishPaymentCreateCommand("payment-create-request", request);
            log.info("[KafkaConsumer] stockDeductionSuccess : 결제 생성 명령 성공");

    }


    //TODO: 재고차감 실패 이벤트 구독 로직
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = "-dlq"
    )
    @KafkaListener(topics = "stock-deduction-fail", groupId = "orchestration-consumer-group")
    public void stockDeductionFail(String stockDeductionFailMessage) throws JsonProcessingException {
        log.info("[KafkaConsumer] stockDeductionFail :  stockDeductionFailMessage: {}", stockDeductionFailMessage);

            StockDeductionFailMessage message = objectMapper.readValue(stockDeductionFailMessage, StockDeductionFailMessage.class);
            if(message == null){
                throw new FailToConvertMessageException();
            }
            log.info("재고 차감 실패 이벤트 구독 후 handler유입 전 StockDeductionFailMessage sagaId : {} ", message.sagaId());

            OrderDeleteCommandRequest orderRequest = adapter.toOrderDeleteCommand(message);
            CouponRestoreCommandRequest couponRequest = adapter.toCouponRestoreCommand(message);

            //TODO: try-catch 예외처리 세분화
            log.info("재고 차감 실패 이벤트 구독 후 handler유입 전 OrderDeleteCommandRequest sagaId : {} ", orderRequest.sagaId());
            orderCreateSagaService.handlerStockDeductionFailed(orderRequest.sagaId(), message.reason());
            log.info("보상 트랜잭션 실행 전");
            orderCreateSagaService.stockDeductionFailedCompensation(orderRequest);

            publisher.publishCouponRestoreCommand("coupon-restore-request",couponRequest);
            log.info("[KafkaConsumer] stockDeductionFail : 쿠폰 사용 복원 이벤트 발행 성공");

            publisher.publishOrderDeleteCommand("order-delete-request", orderRequest);
            log.info("[KafkaConsumer] stockDeductionFail : 주문 삭제 이벤트 발행 성공");


    }

    @KafkaListener(topics = "payment-create-success", groupId = "orchestration-consumer-group")
    public void paymentCreateSuccess(String paymentCreateSuccessMessage) throws JsonProcessingException {
        log.info("[KafkaConsumer] paymentCreateSuccess  :  paymentCreateSuccessMessage: {}", paymentCreateSuccessMessage);

            PaymentCreateSuccessMessage message = objectMapper.readValue(paymentCreateSuccessMessage, PaymentCreateSuccessMessage.class);

            log.info("[KafkaConsumer] paymentCreateSuccess - sagsId : {}, userId : {}, orderId : {}",message.sagaId(), message.userId(), message.orderId());
            ClearCartCommandRequest cartRequest = adapter.toClearCartCommand(message);
            ProcessOrderSuccessCommandRequest orderRequest = adapter.toProcessOrderSuccessCommand(message);

            orderCreateSagaService.handlerPaymentCreateSuccess(message.sagaId(), message.orderId());

            publisher.publishClearCartCommand("cart-clear-request", cartRequest);
            log.info("[OrderCreateSagaService] handlerPaymentCreateSuccess : 장바구니 비우기 명령 성공, sagaId : {}, orderId : {}, userId : {}", cartRequest.sagaId(), cartRequest.orderId(), cartRequest.userId());

            //고민중..
            // publisher.publishOrderSuccessProcessingCommand("order-process-success", orderRequest);
            //log.info("[OrderCreateSagaService] handlerPaymentCreateSuccess : 주문 성공 상태 변경 명령 성공");

            orderCreateSagaService.endOrderCreateSaga(message.sagaId());

    }


    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltTopicSuffix = "-dlq"
    )
    @KafkaListener(topics = "payment-create-fail", groupId = "orchestration-consumer-group")
    public void paymentCreateFail(String paymentCreateFailMessage) throws JsonProcessingException {
        log.info("[KafkaConsumer] paymentCreateFail  :  paymentCreateFailMessage: {}", paymentCreateFailMessage);

            PaymentCreateFailMessage message = objectMapper.readValue(paymentCreateFailMessage, PaymentCreateFailMessage.class);

            if(message == null){
                throw new FailToConvertMessageException();
            }

            OrderDeleteCommandRequest orderRequest = adapter.toOrderDeleteCommand(message);
            StockRestoreCommandRequest stockRequest = adapter.toStockRestoreCommand(message);
            CouponRestoreCommandRequest couponRequest = adapter.toCouponRestoreCommand(message);

            log.info("[KafkaConsumer] paymentCreateFail : request.sagaId : {} ", orderRequest.sagaId());

            orderCreateSagaService.handlerPaymentCreateFailed(message.sagaId(), message.reason());
            orderCreateSagaService.PaymentCreateFailedCompensation(orderRequest, stockRequest);

            publisher.publishStockRestoreCommand("stock-restore-request", stockRequest);
            log.info("[KafkaConsumer] paymentCreateFail : 재고 복원 이벤트 발행 성공");

            publisher.publishCouponRestoreCommand("coupon-restore-request", couponRequest);
            log.info("[KafkaConsumer] paymentCreateFail : 쿠폰 복원 이벤트 발행 성공");

            publisher.publishOrderDeleteCommand("order-delete-request", orderRequest);
            log.info("[KafkaConsumer] paymentCreateFail : 주문 삭제 이벤트 발행 성공");

    }

}
