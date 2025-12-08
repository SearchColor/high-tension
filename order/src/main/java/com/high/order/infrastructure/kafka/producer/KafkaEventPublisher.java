package com.high.order.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}
