package com.high.coupon.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.port.out.dto.CouponIssueCreateMessage;
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
import org.springframework.stereotype.Component;

/**
 * 쿠폰 발급 Consumer - Redis 검증을 통과한 발급 요청 DB에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueConsumer {

    private final ObjectMapper objectMapper;
    private final CouponIssueService couponIssueService;

    /**
     * 쿠폰 발급 요청 메세지 구독
     * - Infrastructure Layer
     * - Kafka 토픽: coupon-issue-request
     * - Consumer Group: coupon-issue-group
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            exclude = {JsonProcessingException.class, SerializationException.class, NullPointerException.class}
    )
    @KafkaListener(topics = "coupon-issue-request",
            groupId = "coupon-issue-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeCouponIssue(String message) {

        try {
            // 파싱
            CouponIssueCreateMessage dto = objectMapper.readValue(message,
                    CouponIssueCreateMessage.class);

            log.info("[COUPON ISSUE] 발급 요청 수신: userId={}, couponId={}", dto.userId(),
                    dto.couponId());

            // 비즈니스 로직 수행 - 실패 시 예외 던짐 -> @RetryableTopic이 잡아서 재시도 토픽으로 보냄
            couponIssueService.saveCouponIssue(dto.couponId(), dto.userId());

            log.info("[COUPON ISSUE] 발급 성공 완료 userId={}", dto.userId());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }


    /**
     * DLT 핸들러
     */
    @DltHandler
    public void dltHandler(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            CouponIssueCreateMessage dto = objectMapper.readValue(message, CouponIssueCreateMessage.class);

            log.error("""
                    🚨 [COUPON ISSUE FAILED DLT] 쿠폰 발급 실패
                     - 실패 토픽 : {}
                     - 쿠폰 ID    : {}
                     - 유저 ID    : {}
                     - 원본 메시지 : {}
                    """,
                    topic,
                    dto.couponId(),
                    dto.userId(),
                    message
            );

            // TODO: 필요시 '발급 실패' 알림 전송 로직 추가될 수 있음

        } catch (Exception e) {
            log.error("DLT 메시지 파싱 실패. 메세지: {}", message, e);
        }
    }
}
