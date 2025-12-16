package com.high.coupon.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.infrastructure.kafka.dto.CouponIssueCreateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 발급 Producer - 발급 요청 메세지 토픽 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(String topic, CouponIssueCreateMessage message){

        try {
            // 객체 -> Json String으로 변환
            String jsonMessage = objectMapper.writeValueAsString(message);

            kafkaTemplate.send(topic, jsonMessage);
            log.info("[COUPON PRODUCER] 쿠폰 발급 메시지 전송 성공. topic={}, payload={}", topic, jsonMessage);

        } catch (JsonProcessingException e){
            log.error("[COUPON PRODUCER] 쿠폰 발급 JSON 변환 실패. message={}", message, e);
            throw new RuntimeException("메시지 변환에 실패하였습니다.", e);
        }
    }

}
