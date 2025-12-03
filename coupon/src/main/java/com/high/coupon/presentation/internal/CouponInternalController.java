package com.high.coupon.presentation.internal;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.dto.response.UserCouponResponse;
import com.library.module.response.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/coupons")
@RequiredArgsConstructor
public class CouponInternalController {

    private final CouponIssueService couponIssueService;

    // todo API 추가 internal 명세서 정리 필요

    /**
     * 사용자 별 보유 쿠폰 조회
     * GET /api/v1/internal/coupons?userId={userId}
     */
    @GetMapping
    public ApiResponse<List<UserCouponResponse>> getUserCoupons(@RequestParam UUID userId) {
        List<UserCouponResponse> result = couponIssueService.getUserCoupons(userId);
        return ApiResponse.success(result);
    }

    /**
     * 쿠폰 사용 처리 (상태 변경)
     * PUT /api/v1/internal/coupons/{couponIssueId}/use?userId={userId}
     */
    @PutMapping("/{couponIssueId}/use")
    public ApiResponse<Void> useCouponInternal(
            @PathVariable UUID couponIssueId,
            @RequestParam UUID userId
    ) {
        couponIssueService.useCoupon(couponIssueId, userId);
        return ApiResponse.success(null);
    }



    // todo ---- 할인 금액 계산 쿠폰쪽에서 필요할지 피드백 필요
    // 필요시에도 계산 로직 수정이 필요할 거 같아서 따로 백업
}
