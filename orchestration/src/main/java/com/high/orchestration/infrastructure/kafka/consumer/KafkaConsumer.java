package com.high.orchestration.infrastructure.kafka.consumer;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

        private final OrderCreateSagaService orderCreateSagaService;
        private final ObjectMapper objectMapper;
        private final OrderCreateAdapter adapter;
        private final KafkaEventPublisher publisher;

    @KafkaListener(topics = "order-create-success")
    public void orderCreateSuccess(String orderCreateSuccessMessage) {
        log.info("[KafkaConsumer] orderCreateSuccess : orderCreateSuccessMessage: {}", orderCreateSuccessMessage);

        try {
            OrderCreateSuccessMessage message = objectMapper.readValue(orderCreateSuccessMessage, OrderCreateSuccessMessage.class);
            StockDeductionCommandRequest stockRequest = adapter.toStockDeductionCommand(message);
            CouponUseCommandRequest couponRequest = adapter.toCouponUseCommandRequest(message);
            orderCreateSagaService.handlerOrderCreateSuccess(stockRequest);

            publisher.publishCouponUseCommand("coupon-use-request", couponRequest);
            publisher.publishStockDeductionCommand("stock-deduction-request", stockRequest);
            log.info("[KafkaConsumer] orderCreateSuccess : 재고차감 명령 발행 성공 ");


            log.info("ID: {}", message.userId());
            log.info("ID : {}, ID: {}", stockRequest.userId(), couponRequest.userId());
        } catch (Exception e) {
            log.error("[KafkaConsumer] orderCreateSuccess : 메시지 파싱 실패 : {}", orderCreateSuccessMessage, e);
            }
    }

    @KafkaListener(topics = "order-create-fail")
    public void orderCreateFail(String orderCreateFailMessage) {
        log.info("[kafkaConsumer] orderCreateFail : orderCreateFailMessage {}", orderCreateFailMessage);
        try {
            OrderCreateFailedMessage message = objectMapper.readValue(orderCreateFailMessage, OrderCreateFailedMessage.class);
            OrderCreateFailCommandResponse request = adapter.toOrderCreateFailCommand(message);
            orderCreateSagaService.handleOrderCreateFailed(request);
            log.info("주문 생성 실패 메시지 처리 완료");
        } catch (Exception e) {
            log.error("주문 실패 메시지 파싱 실패");
        }
    }


    @KafkaListener(topics = "stock-deduction-success")
    public void stockDeductionSuccess(String stockDeductionSuccessMessage) {
        log.info("[KafkaConsumer] stockDeductionSuccess :  stockDeductionSuccessMessage: {}", stockDeductionSuccessMessage);

        try {
            StockDeductionSuccessMessage message = objectMapper.readValue(stockDeductionSuccessMessage, StockDeductionSuccessMessage.class);
            PaymentCreateCommandRequest request = adapter.toPaymentCreateCommand(message);
            orderCreateSagaService.handlerStockDeductionSuccess(request);

            publisher.publishPaymentCreateCommand("payment-create-request", request);
            log.info("[KafkaConsumer] stockDeductionSuccess : 결제 생성 명령 성공");
        } catch (Exception e) {
            log.error("[KafkaConsumer] stockDeductionSuccess : 메시지 파싱 실패 : {}", stockDeductionSuccessMessage, e);
        }

    }

    //TODO: 재고차감 실패 이벤트 구독 로직
    @KafkaListener(topics = "stock-deduction-fail")
    public void stockDeductionFail(String stockDeductionFailMessage) {
        log.info("[KafkaConsumer] stockDeductionFail :  stockDeductionFailMessage: {}", stockDeductionFailMessage);

        try {

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

        } catch (Exception e) {
            log.error("[KafkaConsumer] stockDeductionFail : 재고 차감 실패 메시지 처리 실패 : {}", stockDeductionFailMessage, e);
        }

    }

    @KafkaListener(topics = "payment-create-success")
    public void paymentCreateSuccess(String paymentCreateSuccessMessage) {
        log.info("[KafkaConsumer] paymentCreateSuccess  :  paymentCreateSuccessMessage: {}", paymentCreateSuccessMessage);

        try {
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


        } catch (Exception e) {
            log.error("[KafkaConsumer] paymentCreateSuccess : 메시지 파싱 실패 : {}", paymentCreateSuccessMessage, e);

        }
    }

    @KafkaListener(topics = "payment-create-fail")
    public void paymentCreateFail(String paymentCreateFailMessage) {
        log.info("[KafkaConsumer] paymentCreateFail  :  paymentCreateFailMessage: {}", paymentCreateFailMessage);

        try {
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

        } catch (Exception e) {
            log.error("[KafkaConsumer] paymentCreateFail : 결제 생성 실패 메시지 처리 실패 : {}", paymentCreateFailMessage, e);

        }
    }

}
