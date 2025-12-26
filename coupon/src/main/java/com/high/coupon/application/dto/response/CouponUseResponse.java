package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.CouponIssue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "쿠폰 사용 응답 DTO")
public record CouponUseResponse(
        UUID couponIssueId,
        UUID userId,
        Boolean isUsed,
        LocalDateTime usedAt
) {
    public static CouponUseResponse from(CouponIssue couponIssue) {
        return new CouponUseResponse(
                couponIssue.getId(),
                couponIssue.getUserId(),
                couponIssue.getIsUsed(),
                couponIssue.getUsedAt()
        );
    }
}
