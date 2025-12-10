package com.high.user.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 쿠폰 정보 응답 DTO
 * Application 계층에서 사용하는 도메인 무관 DTO
 */
public record CouponResponse(
        UUID couponIssueId,
        UUID couponId,
        String couponName,
        BigDecimal discountRate,
        LocalDateTime validStartAt,
        LocalDateTime validEndAt,
        Boolean isUsed
) {
}