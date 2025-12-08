package com.high.orchestration.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.OrderCreateSagaService;
import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.response.OrderCreateFailCommandResponse;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.infrastructure.adaptor.OrderCreateAdapter;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateFailedMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.StockDeductionSuccessMessage;
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

        @KafkaListener(topics = "order-create-success")
        public void orderCreateSuccess(String orderCreateSuccessMessage) {
            log.info("[KafkaConsumer] orderCreateSuccess : orderCreateSuccessMessage: {}", orderCreateSuccessMessage);

            try {
                OrderCreateSuccessMessage message = objectMapper.readValue(orderCreateSuccessMessage, OrderCreateSuccessMessage.class);
                StockDeductionCommandRequest request = adapter.toStockDeductionCommand(message);
                orderCreateSagaService.handlerOrderCreateSuccess(request);
            } catch (Exception e) {
                log.error("[KafkaConsumer] orderCreateSuccess : 메시지 파싱 실패 : {}", orderCreateSuccessMessage, e);
            }
        }

        //TODO: 주문 생성 실패 이벤트 구독 로직
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
            } catch (Exception e) {
                log.error("[KafkaConsumer] stockDeductionSuccess : 메시지 파싱 실패 : {}", stockDeductionSuccessMessage, e);
            }

        }

        //TODO: 재고차감 실패 이벤트 구독 로직

        @KafkaListener(topics = "payment-create-success")
        public void paymentCreateSuccess(String paymentCreateSuccessMessage) {
            log.info("[KafkaConsumer] paymentCreateSuccess  :  paymentCreateSuccessMessage: {}", paymentCreateSuccessMessage);

            try {
                PaymentCreateSuccessMessage message = objectMapper.readValue(paymentCreateSuccessMessage, PaymentCreateSuccessMessage.class);
                ClearCartCommandRequest request = adapter.toClearCartCommand(message);
                orderCreateSagaService.handlerPaymentCreateSuccess(request);
            } catch (Exception e) {
                log.error("[KafkaConsumer] paymentCreateSuccess : 메시지 파싱 실패 : {}", paymentCreateSuccessMessage, e);

            }
        }

}
