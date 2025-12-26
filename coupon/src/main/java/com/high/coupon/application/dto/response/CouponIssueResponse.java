package com.high.coupon.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Kafka 사용자 응답용
 */
@Schema(description = "쿠폰 발급 결과 DTO")
public record CouponIssueResponse(
        boolean isSuccess,
        String message
) {
}
