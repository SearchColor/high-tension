package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.CouponIssue;
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
    public static UserCouponResponse from(CouponIssue issue) {
        return new UserCouponResponse(
                issue.getId(),
                issue.getCoupon().getId(),
                issue.getCoupon().getName(),
                issue.getCoupon().getDiscountRate(),
                issue.getValidStartAt(),
                issue.getValidEndAt(),
                issue.getIsUsed()
        );
    }
}
