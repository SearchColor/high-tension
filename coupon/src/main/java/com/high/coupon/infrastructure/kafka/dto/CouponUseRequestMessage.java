package com.high.coupon.infrastructure.kafka.dto;

import java.util.UUID;

/**
 * todo 필드 맞춰야함 (필요 필드 확인 필요)
 * Kafka 메세지 역직렬화를 위한 DTO
 * - Kafka에서 오는 json 메세지 구조와 일치 시켜야함
 * ObjectMapper (String -> UUID 변환)
 */
public record CouponUseRequestMessage(
        UUID couponIssueId, // 쿠폰 발급 ID
        UUID userId,       // 쿠폰 사용자
        UUID sagaId
){
}
