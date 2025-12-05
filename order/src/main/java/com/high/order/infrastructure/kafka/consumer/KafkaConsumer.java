package com.high.order.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.order.application.dto.internal.kafka.request.CreateOrderCommand;
import com.high.order.application.dto.internal.kafka.response.OrderSuccessResponse;
import com.high.order.application.port.EventPublisher;
import com.high.order.application.service.OrderServiceV2;
import com.high.order.infrastructure.adapter.OrderCreateAdapter;
import com.high.order.infrastructure.exception.EmptyKafkaMessageException;
import com.high.order.infrastructure.kafka.dto.response.OrderCreateRequestMessage;
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
        log.info("Kafka Message : {}", message);
        OrderCreateRequestMessage orderCreateRequestMessage = null;
        objectMapper = new ObjectMapper();

        try {
            orderCreateRequestMessage = objectMapper.readValue(message, OrderCreateRequestMessage.class);
            System.out.println(objectMapper.writeValueAsString(orderCreateRequestMessage));

            if(orderCreateRequestMessage == null) {
                log.info("[Kafka Consumer] handleOrderCreateRequest : 메시지가 비어있음");
                throw new EmptyKafkaMessageException();
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            CreateOrderCommand command = adapter.toCommand(orderCreateRequestMessage);
            log.info("[KafkaConsumer] handleOrderCreateRequest : 주문 생성 로직 실행");
            OrderSuccessResponse response = orderService.createOrder(command);

            publisher.sendOrderCreateSuccess("order-success-create", response);
            log.info("[KafkaConsumer] handlerOrderCreatRequest : 주문 생성 성공 메시지 생성");
        } catch (Exception e) {

            //TODO: 주문 생성 실패시 로직 및 실패 메시지 발행
            log.info("[KafkaConsumer] handleOrderCreateRequest : 주문 생성 로직 실행 실패 / 성공 메시지 발행 요청 실패");
        }

    }
}
