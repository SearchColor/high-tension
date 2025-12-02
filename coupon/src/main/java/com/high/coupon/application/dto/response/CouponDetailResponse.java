package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.Coupon;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponDetailResponse(
        UUID couponId,
        String name,
        String description,
        BigDecimal discountRate,
        Integer totalQuantity,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime validUntil,
        LocalDateTime createdAt,
        // todo String -> UUID
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
    public static CouponDetailResponse from(Coupon coupon) {
        return new CouponDetailResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDescription(),
                coupon.getDiscountRate(),
                coupon.getTotalQuantity(),
                coupon.getIssueStartAt(),
                coupon.getIssueEndAt(),
                coupon.getValidUntil(),
                coupon.getCreatedAt(),
                coupon.getCreatedBy(),
                coupon.getUpdatedAt(),
                coupon.getUpdatedBy()
        );
    }
}
