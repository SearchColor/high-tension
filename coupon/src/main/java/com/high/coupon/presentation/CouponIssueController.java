package com.high.coupon.presentation;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.dto.response.CouponIssueResponse;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    // 쿠폰 발급 (일반 유저)
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<ApiResponse<CouponIssueResponse>> issueCoupon(
            @PathVariable UUID couponId
    ){
        UUID userId = SecurityContextUtil.getCurrentUserId();
        CouponIssueResponse response = couponIssueService.issueCoupon(couponId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    /**
     * todo: 테스트용 URL
     */
    @PostMapping("/{couponId}/issue/test")
    public ResponseEntity<CouponIssueResponse> issueCouponTest(
            @PathVariable UUID couponId,
            @RequestParam UUID userId
    ){
        CouponIssueResponse response = couponIssueService.issueCoupon(couponId, userId);
        return ResponseEntity.ok(response);
    }
}
