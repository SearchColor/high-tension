package com.high.coupon.presentation.internal;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.dto.response.CouponUseResponse;
import com.high.coupon.application.dto.response.CouponValidationResponse;
import com.high.coupon.application.dto.response.UserCouponResponse;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/coupons")
@RequiredArgsConstructor
public class CouponInternalController {

    private final CouponIssueService couponIssueService;

    // todo API 추가 internal 명세서 정리 필요 - param 제거 확인 path 수정

    /**
     * 사용자 별 보유 쿠폰(사용 가능 상태) 조회
     * GET /api/v1/internal/coupons?userId={userId}
     */
    @GetMapping
    public ApiResponse<List<UserCouponResponse>> getUserCoupons() {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        List<UserCouponResponse> response = couponIssueService.getAvailableUserCoupons(userId);
        return ApiResponse.success(response);
    }

    /**
     * 쿠폰 단건 유효성 검증 및 할인 정보
     * GET /api/v1/internal/coupons/{couponIssueId}/validate?userId={userId}
     */
    @GetMapping("/{couponIssueId}/validate")
    public ApiResponse<CouponValidationResponse> validateCoupon(@PathVariable UUID couponIssueId) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        CouponValidationResponse response = couponIssueService.validateCoupon(couponIssueId, userId);
        return ApiResponse.success(response);
    }

    /**
     * 쿠폰 사용 처리 (상태 변경)
     * PUT /api/v1/internal/coupons/{couponIssueId}/use?userId={userId}
     */
    @PutMapping("/{couponIssueId}/use")
    public ApiResponse<CouponUseResponse> useCoupon(@PathVariable UUID couponIssueId) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        CouponUseResponse response = couponIssueService.useCoupon(couponIssueId, userId);
        return ApiResponse.success(response);
    }

    /**
     * 쿠폰 복원 처리 (주문/결제 취소 시 유효기간 이내 재발급)
     * POST /api/v1/internal/coupons/{couponIssuedId}/restore
     */
    @PostMapping("/{couponIssueId}/restore")
    public ApiResponse<CouponUseResponse> restoreCoupon(
            @PathVariable UUID couponIssueId) {

        CouponUseResponse response = couponIssueService.restoreCoupon(couponIssueId);
        return ApiResponse.success(response);
    }
}
