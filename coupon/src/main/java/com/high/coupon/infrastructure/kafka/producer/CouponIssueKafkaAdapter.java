package com.high.coupon.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.application.port.out.CouponIssueEventPort;
import com.high.coupon.application.port.out.dto.CouponIssueCreateMessage;
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
public class CouponIssueKafkaAdapter implements CouponIssueEventPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishIssueRequest(CouponIssueCreateMessage message){

        try {
            // 객체 -> Json String으로 변환
            String jsonMessage = objectMapper.writeValueAsString(message);

            kafkaTemplate.send("coupon-issue-request", jsonMessage)
                            .whenComplete((result, ex) -> {
                                if (ex == null){
                                    log.info("[COUPON PRODUCER] 쿠폰 발급 메시지 전송 성공. topic={}, offset={}",
                                            result.getRecordMetadata().topic(), result.getRecordMetadata().offset());
                                } else {
                                    log.error("[COUPON PRODUCER] 쿠폰 발급 메시지 전송 실패 payload={}", jsonMessage, ex);
                                }
                            });

        } catch (JsonProcessingException e){
            log.error("[COUPON PRODUCER] 쿠폰 발급 JSON 변환 실패. message={}", message, e);
            throw new RuntimeException("메시지 변환에 실패하였습니다.", e);
        }
    }
}
