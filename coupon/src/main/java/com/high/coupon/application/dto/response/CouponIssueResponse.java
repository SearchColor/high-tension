package com.high.coupon.application.dto.response;

/**
 * Kafka 사용자 응답용
 */
public record CouponIssueResponse(
        boolean isSuccess,
        String message
) {
}
