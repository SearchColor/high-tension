package com.high.coupon.presentation;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.dto.response.CouponIssueResponse;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CouponIssue", description = "쿠폰 발급 API (USER 권한만 쿠폰 발급이 가능합니다.)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    // 쿠폰 발급 (일반 유저)
    @Operation(summary = "쿠폰 발급", description = "쿠폰 발급 요청")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<ApiResponse<CouponIssueResponse>> issueCoupon(
            @PathVariable UUID couponId
    ){
        UUID userId = SecurityContextUtil.getCurrentUserId();
        var response = couponIssueService.issueCoupon(couponId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

//
//    /**
//     * todo: 테스트용 URL
//     */
//    @PostMapping("/{couponId}/issue/test")
//    public ResponseEntity<CouponIssueResponse> issueCouponTest(
//            @PathVariable UUID couponId,
//            @RequestBody TestCouponRequest request // 아래 만든 record 사용
//    ) {
//        // 서비스 호출 (userId를 Body에서 꺼내서 넘김)
//        var response = couponIssueService.issueCoupon(couponId, request.userId());
//        return ResponseEntity.ok(response);
//    }
//
//    public record TestCouponRequest(UUID userId) {}
}
