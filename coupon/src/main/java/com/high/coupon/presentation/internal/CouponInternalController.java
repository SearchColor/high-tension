package com.high.coupon.presentation.internal;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.dto.response.UserCouponResponse;
import com.library.module.response.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/coupons")
@RequiredArgsConstructor
public class CouponInternalController {

    private final CouponIssueService couponIssueService;

    /**
     * 사용자 별 보유 쿠폰 조회
     * GET /api/v1/internal/coupons?userId={userId}
     */
    @GetMapping
    public ApiResponse<List<UserCouponResponse>> getUserCoupons(@RequestParam UUID userId) {
        List<UserCouponResponse> result = couponIssueService.getUserCoupons(userId);
        return ApiResponse.success(result);
    }

}
