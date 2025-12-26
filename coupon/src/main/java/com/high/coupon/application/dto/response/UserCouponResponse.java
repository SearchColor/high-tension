package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.CouponIssue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "사용자 별 보유 쿠폰 응답 DTO")
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
