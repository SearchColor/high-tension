package com.high.order.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.order.application.dto.internal.kafka.response.OrderCreateFailedResponse;
import com.high.order.application.dto.internal.kafka.response.OrderDeleteResponse;
import com.high.order.application.dto.internal.kafka.response.OrderSuccessResponse;
import com.high.order.application.port.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendOrderCreateSuccess(String topic, OrderSuccessResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "";

        try {
            jsonString = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonString);
        log.info("[KafkaProducer] sendOrderCreateSuccess : 이벤트 발행 성공");
    }




    public void sendOrderCreateFail(String topic, OrderCreateFailedResponse orderCreateFailedResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "";

        try {
            jsonString = objectMapper.writeValueAsString(orderCreateFailedResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        kafkaTemplate.send(topic, jsonString);
        log.info("[KafkaProducer] sendOrderCreateFail : 이벤트 발행 성공");

    }

    @Override
    public void sendOrderDeleteSuccess(String topic, OrderDeleteResponse orderDeleteResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "";

        try {
            jsonString = objectMapper.writeValueAsString(orderDeleteResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        kafkaTemplate.send(topic, jsonString);
        log.info("[KafkaProducer] sendOrderDeleteSuccess : 이벤트 발행 성공");
    }

    @Override
    public void sendOrderDeleteFail(String topic, OrderDeleteResponse orderDeleteResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "";

        try {
            jsonString = objectMapper.writeValueAsString(orderDeleteResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        kafkaTemplate.send(topic, jsonString);
        log.info("[KafkaProducer] sendOrderDeleteFail : 이벤트 발행 성공");
    }

}
