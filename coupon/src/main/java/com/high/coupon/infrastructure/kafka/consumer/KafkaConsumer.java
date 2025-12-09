package com.high.coupon.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.application.CouponIssueService;
import com.high.coupon.infrastructure.kafka.dto.CouponUseRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Kafka 토픽 구독 Consumer - coupon-use-request 토픽 메세지를 받음
 * todo 연동 테스트를 위해 컨슈머 기본 로직만 작성 (성공/실패 응답 로직 필요 여부 확인)
 * 실패 시 로그만 남기고 ack 처리하도록 구현
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper;

    /**
     * 쿠폰 사용 전환 요청 메시지 구독
     * - Infrastructure Layer
     * - Kafka 토픽: coupon-use-request
     * - Consumer Group: coupon-service-group
     */
    @KafkaListener(
            topics = "coupon-use-request",
            groupId = "coupon-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCouponRequest(String message, Acknowledgment ack) {
        log.info("[SAGA COUPON CONSUMER] 수신 메시지: {}", message);
        CouponUseRequestMessage dto = null;

        try {
            // JSON → DTO 역직렬화 (UUID 타입)
            dto = objectMapper.readValue(message, CouponUseRequestMessage.class);
            log.info("파싱 완료 sagaId: {}", dto.sagaId());

            // orderId 기반 wrapper 호출 (service)
            couponIssueService.useCouponByOrderId(dto.orderId());

            // 처리 성공 시 알림
            log.info("[SAGA COUPON CONSUMER] 처리 성공 orderId={}, sagaId={}", dto.orderId(), dto.sagaId());

        } catch (Exception e) {
            log.error("[SAGA COUPON CONSUMER] 처리 실패 - 메시지 건너뜀. 내용: {}, 에러: {}", message, e.getMessage()); // todo: KafkaConfig DLQ 도입 필요

        } finally {
            // 성공하든 실패하든 무조건 커밋(Ack) 후 다음 메시지를 받을 준비
            ack.acknowledge();
        }
    }
}
