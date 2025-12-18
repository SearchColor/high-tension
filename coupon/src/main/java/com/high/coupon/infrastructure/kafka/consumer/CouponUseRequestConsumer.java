package com.high.coupon.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.application.CouponIssueService;
import com.high.coupon.infrastructure.kafka.dto.CouponUseRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

/**
 * Kafka 토픽 구독 Consumer - coupon-use-request 토픽 메세지를 받음
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CouponUseRequestConsumer {

    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper;

    /**
     * 쿠폰 사용 전환 요청 메시지 구독
     * - Infrastructure Layer
     * - Kafka 토픽: coupon-use-request
     * - Consumer Group: coupon-service-group
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            exclude = {JsonProcessingException.class, SerializationException.class, NullPointerException.class}
    )
    @KafkaListener(
            topics = "coupon-use-request",
            groupId = "coupon-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCouponRequest(String message) { //  Acknowledgment ack 제거

        log.info("[SAGA COUPON CONSUMER: use req] 수신 메시지: {}", message);
        CouponUseRequestMessage dto = null;

        try {
            // JSON → DTO 역직렬화 (UUID 타입)
            dto = objectMapper.readValue(message, CouponUseRequestMessage.class);
            log.info("[SAGA COUPON CONSUMER: use req] 파싱 완료 sagaId: {}", dto.sagaId());

            // orderId 기반 wrapper 호출 (service)
            couponIssueService.useCouponByOrderId(dto.orderId());

            // 처리 성공 시 알림
            log.info("[SAGA COUPON CONSUMER: use req] 처리 성공 orderId={}, sagaId={}, userId={}", dto.orderId(), dto.sagaId(), dto.userId());

        } catch (JsonProcessingException e) {

            // 재시도 X
            throw new RuntimeException("Json 파싱 실패", e);

        } catch (Exception e){

            // 실패 시 재시도 토픽으로 보내는 예외
            log.error("[SAGA COUPON CONSUMER: use req] 처리 실패 내용: {}, 에러: {}", message, e.getMessage());
            throw new RuntimeException("Kafka Consumer Error", e);

        }
    }

    /**
     * DLT 핸들러 (최종 실패 시 실행)
     */
    @DltHandler
    public void dltHandler(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {
            CouponUseRequestMessage dto = objectMapper.readValue(message, CouponUseRequestMessage.class);

            log.error("""
                🚨 [COUPON USE FAILED DLT] 쿠폰 사용 실패
                 - 실패 토픽 : {}
                 - 주문 ID    : {}
                 - 유저 ID    : {}
                 - 메시지 : {}
                """,
                    topic,
                    dto.orderId(),
                    dto.userId(),
                    message
            );

            // TODO: 필요시 '발급 실패' 알림 전송 로직 추가될 수 있음

        } catch (Exception e) {
            log.error("DLT 파싱 실패. 메세지: {}", message);
        }
    }
}
