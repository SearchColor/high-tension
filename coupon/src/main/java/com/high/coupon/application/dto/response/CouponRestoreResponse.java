package com.high.coupon.application.dto.response;

import com.high.coupon.domain.entity.CouponIssue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "쿠폰 복원 응답 DTO")
public record CouponRestoreResponse(
        UUID couponIssueId,
        UUID userId,
        Boolean isUsed, // 상태
        LocalDateTime usedAt, // 사용 시간 초기화
        LocalDateTime validStartAt,
        LocalDateTime ValidEndAt,
        LocalDateTime updatedAt
) {
    public static CouponRestoreResponse from(CouponIssue couponIssue){
        return new CouponRestoreResponse(
                couponIssue.getId(),
                couponIssue.getUserId(),
                couponIssue.getIsUsed(),
                couponIssue.getUsedAt(),
                couponIssue.getValidStartAt(),
                couponIssue.getValidEndAt(),
                couponIssue.getUpdatedAt()
        );
    }
}
