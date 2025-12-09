package com.high.user.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserCouponResponse(
        UUID couponIssueId,
        UUID couponId,
        String couponName,
        BigDecimal discountRate,
        LocalDateTime validStartAt,
        LocalDateTime validEndAt,
        Boolean isUsed
) {
}