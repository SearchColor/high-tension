package com.high.coupon.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.application.CouponIssueService;
import com.high.coupon.infrastructure.kafka.dto.CouponIssueCreateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
    @KafkaListener(topics = "coupon-issue-request",
            groupId = "coupon-issue-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeCouponIssue(String message, Acknowledgment ack){
        try {
            // 메세지 파싱
            CouponIssueCreateMessage dto = objectMapper.readValue(message, CouponIssueCreateMessage.class);
            log.info("[COUPON ISSUE CONSUMER]: userId={}, couponId={}", dto.userId(), dto.couponId());

            // DB 저장
            couponIssueService.saveCouponIssue(dto.couponId(), dto.userId());

            log.info("[COUPON ISSUE CONSUMER] DB 저장 완료. userId={}, couponId={}", dto.userId(), dto.couponId());

            ack.acknowledge();

        } catch (Exception e) {
            log.error("[COUPON ISSUE CONSUMER] 실패 {}", e.getMessage());
            ack.acknowledge();
            // TODO: 실패 시 재시도 로직이나 DLQ(Dead Letter Queue) 처리 필요
        }
    }
}
