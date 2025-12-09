package com.high.coupon.infrastructure.kafka.dto;

import java.util.UUID;

/**
 * Kafka 메세지 역직렬화를 위한 DTO
 * - Kafka에서 오는 json 메세지 구조와 일치 시켜야함
 * ObjectMapper (String -> UUID 변환)
 */
public record CouponUseRequestMessage(
        UUID sagaId,
        UUID orderId
){
}
