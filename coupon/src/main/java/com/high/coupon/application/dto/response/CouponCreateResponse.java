package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.Coupon;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponCreateResponse(
        UUID id,
        String name,
        String description,
        BigDecimal discountRate,
        Integer totalQuantity,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime validUntil,
        LocalDateTime createdAt,
        UUID createdBy
) {
    public static CouponCreateResponse from(Coupon coupon){
        return new CouponCreateResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDescription(),
                coupon.getDiscountRate(),
                coupon.getTotalQuantity(),
                coupon.getIssueStartAt(),
                coupon.getIssueEndAt(),
                coupon.getValidUntil(),
                coupon.getCreatedAt(),
                coupon.getCreatedBy()
        );
    }
}
