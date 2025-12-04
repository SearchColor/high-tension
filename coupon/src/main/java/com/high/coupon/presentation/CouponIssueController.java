package com.high.coupon.presentation;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.dto.response.CouponIssueResponse;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    // todo 권한 검증 필요 + userId @Authentication (테스트용 AuditorAware 사용)

    // 쿠폰 발급
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<ApiResponse<CouponIssueResponse>> issueCoupon(
            @PathVariable UUID couponId
    ){
        CouponIssueResponse response = couponIssueService.issueCoupon(couponId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
