package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.CouponIssue;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 쿠폰 검증 응답 DTO
 */
public record CouponValidationResponse(
        UUID couponIssueId,
        UUID couponId,
        String couponName,
        BigDecimal discountRate
) {
    public static CouponValidationResponse from(CouponIssue couponIssue){
        return new CouponValidationResponse(
                couponIssue.getId(),
                couponIssue.getCoupon().getId(),
                couponIssue.getCoupon().getName(),
                couponIssue.getCoupon().getDiscountRate()
        );
    }
}
