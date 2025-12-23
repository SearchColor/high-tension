package com.high.user.infrastructure.client;

import com.high.user.infrastructure.client.dto.UserCouponResponse;
import com.high.user.infrastructure.config.FeignConfig;
import com.library.module.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "coupon-service",
        configuration = FeignConfig.class,
        fallback = CouponServiceClientFallback.class
)
public interface CouponServiceClient {

    /**
     * 사용자 쿠폰 조회
     * Coupon Service는 X-User-Id 헤더에서 userId를 가져옴 (SecurityContext)
     * FeignConfig.requestInterceptor()에서 자동으로 X-User-Id 헤더 추가
     */
    @GetMapping("/api/v1/internal/coupons")
    ApiResponse<List<UserCouponResponse>> getUserCoupons();
}
