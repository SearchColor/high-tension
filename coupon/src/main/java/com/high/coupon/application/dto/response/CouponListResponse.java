package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.Coupon;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponListResponse(
        UUID couponId,
        String name,
        String description,
        BigDecimal discountRate,
        Integer totalQuantity,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime validUntil
) {
    public static CouponListResponse from(Coupon coupon) {
        return new CouponListResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDescription(),
                coupon.getDiscountRate(),
                coupon.getTotalQuantity(),
                coupon.getIssueStartAt(),
                coupon.getIssueEndAt(),
                coupon.getValidUntil()
        );
    }
}
