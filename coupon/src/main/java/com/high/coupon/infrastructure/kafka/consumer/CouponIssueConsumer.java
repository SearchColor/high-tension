package com.high.coupon.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.port.out.dto.CouponIssueCreateMessage;
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

        CouponIssueCreateMessage dto;
        try {
            dto = objectMapper.readValue(message, CouponIssueCreateMessage.class);
        } catch (Exception e) {
            log.error("[COUPON ISSUE CONSUMER] 메세지 파싱 실패 payload={}", message, e);
            return; // todo DLQ 필요
        }

        try {
            couponIssueService.saveCouponIssue(dto.couponId(), dto.userId());
            ack.acknowledge(); // 성공 시에만 commit
        } catch (Exception e) {
            log.error("[COUPON ISSUE CONSUMER] 쿠폰 발급 실패 → 재시도 대상", e);
        }
    }
}
