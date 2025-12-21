package com.high.order.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.order.application.OrderServiceV2;
import com.high.order.application.dto.event.request.CreateOrderCommand;
import com.high.order.application.dto.event.request.DeleteOrderCommand;
import com.high.order.application.dto.event.response.OrderCreateFailedResponse;
import com.high.order.application.dto.event.response.OrderDeleteResponse;
import com.high.order.application.dto.event.response.OrderSuccessResponse;
import com.high.order.application.port.EventPublisher;
import com.high.order.infrastructure.context.MessageContext;
import com.high.order.infrastructure.exception.EmptyKafkaMessageException;
import com.high.order.infrastructure.kafka.adapter.OrderCreateAdapter;
import com.high.order.infrastructure.kafka.dto.response.OrderCreateRequestMessage;
import com.high.order.infrastructure.kafka.dto.response.OrderDeleteRequestMessage;
import com.high.order.infrastructure.kafka.dto.response.PaymentSuccessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderServiceV2 orderService;
    private ObjectMapper objectMapper;
    private final OrderCreateAdapter adapter;
    private final EventPublisher publisher;

    @KafkaListener(topics = "order-create-request")
    public void handleOrderCreateRequest(String message) {

        log.info("[KafkaConsumer] handleOrderCreateRequest : 주문 생성 로직 실행 - Kafka Message : {}", message);

        OrderCreateRequestMessage orderCreateRequestMessage = null;
        objectMapper = new ObjectMapper();

        try {
            orderCreateRequestMessage = objectMapper.readValue(message, OrderCreateRequestMessage.class);
            System.out.println(objectMapper.writeValueAsString(orderCreateRequestMessage));
            MessageContext.set(orderCreateRequestMessage.ordererId(), "USER");


            if(orderCreateRequestMessage == null) {
                log.info("[Kafka Consumer] handleOrderCreateRequest : 메시지가 비어있음");
                throw new EmptyKafkaMessageException();
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {

            CreateOrderCommand command = adapter.toCreateCommand(orderCreateRequestMessage);
            log.info("command orderId : {}", command.ordererId());

            MessageContext.set(command.ordererId(), "USER"); // 메시지에 담긴 userId 세팅
            OrderSuccessResponse response = orderService.createOrder(command);


            log.info("[주문 생성 성공 메시지 response.sagaId: {}, orderId: {}, userId: {} ", response.sagaId(), response.orderId(), response.userId());
            publisher.sendOrderCreateSuccess("order-create-success", response);
            log.info("[KafkaConsumer] handlerOrderCreatRequest : 주문 생성 성공 메시지 생성");
        } catch (Exception e) {

            //TODO: 주문 생성 실패시 로직 및 실패 메시지 발행
            log.info("[KafkaConsumer] handleOrderCreateRequest : 주문 생성 로직 실행 실패 / 성공 메시지 발행 요청 실패");
            String errorMessage = e.getMessage();

            OrderCreateFailedResponse orderCreateFailedResponse =
                OrderCreateFailedResponse.of(orderCreateRequestMessage.sagaId(),
                    errorMessage, orderCreateRequestMessage.ordererId());

            publisher.sendOrderCreateFail("order-create-fail", orderCreateFailedResponse);
            MessageContext.clear();

        } finally {
            MessageContext.clear();
        }

    }


    @KafkaListener(topics = "order-delete-request")
    public void handleOrderDeleteRequest(String message) {
        log.info("Kafka Message : {}", message);
        OrderDeleteRequestMessage orderDeleteRequestMessage = null;
        objectMapper = new ObjectMapper();
        try {
            orderDeleteRequestMessage = objectMapper.readValue(message,
                OrderDeleteRequestMessage.class);
            System.out.println(objectMapper.writeValueAsString(orderDeleteRequestMessage));

            if (orderDeleteRequestMessage == null) {
                log.info("[Kafka Consumer] handleOrderDeleteRequest : 메시지가 비어있음");
                throw new EmptyKafkaMessageException();
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        DeleteOrderCommand command = adapter.toDeleteCommand(orderDeleteRequestMessage);

        try {
            log.info("[KafkaConsumer] handleOrderDeleteRequest : 주문 삭제 로직 실행");
            orderService.deleteOrder(command.orderId(), orderDeleteRequestMessage.userId(), "USER");
            String resultMessage = command.orderId() + " 삭제 완료 처리";
            OrderDeleteResponse response = OrderDeleteResponse.of(command.sagaId(),
                command.orderId(), resultMessage);

            publisher.sendOrderDeleteSuccess("order-delete-success", response);
            log.info("[KafkaConsumer] handlerOrderCreatRequest : 주문 삭제 성공 메시지 생성");

        } catch (Exception e) {

            //TODO: 주문 삭제 실패시 로직 및 실패 메시지 발행
            log.info(
                "[KafkaConsumer] handleOrderDeleteRequest : 주문 삭제 로직 실행 실패 / 삭제 성공 메시지 발행 요청 실패");
            String errorMessage = e.getMessage();
            OrderDeleteResponse response = OrderDeleteResponse.of(command.orderId(),
                command.sagaId(), errorMessage);
            publisher.sendOrderDeleteFail("order-delete-fail", response);

        }

    }

    @KafkaListener(topics = "payment-create-success")
    public void handlePaymentCreateSuccess(String message) {
        log.info("Kafka Message : {}", message);
        PaymentSuccessMessage paymentSuccessMessage = null;
        objectMapper = new ObjectMapper();
        try {
            paymentSuccessMessage = objectMapper.readValue(message, PaymentSuccessMessage.class);

            if (paymentSuccessMessage == null) {
                log.info("[Kafka Consumer] handleOrderDeleteRequest : 메시지가 비어있음");
                throw new EmptyKafkaMessageException();
            }


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        orderService.processOrderSuccess(paymentSuccessMessage.orderId());

    }

}