package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.CouponIssue;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponIssueResponse(
        UUID couponIssueId,
        UUID couponId,
        UUID userId,
        LocalDateTime issuedAt,
        Boolean isUsed,
        LocalDateTime usedAt,
        LocalDateTime validStartAt,
        LocalDateTime validEndAt
) {
    public static CouponIssueResponse from(CouponIssue couponIssue){
        return new CouponIssueResponse(
                couponIssue.getId(),
                couponIssue.getCoupon().getId(),
                couponIssue.getUserId(),
                couponIssue.getIssuedAt(),
                couponIssue.getIsUsed(),
                couponIssue.getUsedAt(),
                couponIssue.getValidStartAt(),
                couponIssue.getValidEndAt()
        );
    }
}
