package com.high.coupon.infrastructure.kafka.dto;

import java.util.UUID;

/**
 * kafka로 보낼 메세지 객체 - 쿠폰 발급에 필요한 정보만
 */
public record CouponIssueCreateMessage(
        UUID userId,
        UUID couponId
) {
}
