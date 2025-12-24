package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "쿠폰 상세 조회 응답 DTO")
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
        UUID createdBy,
        LocalDateTime updatedAt,
        UUID updatedBy
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
