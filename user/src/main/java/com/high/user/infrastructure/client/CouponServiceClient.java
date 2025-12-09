package com.high.user.infrastructure.client;

import com.high.user.infrastructure.client.dto.UserCouponResponse;
import com.high.user.infrastructure.config.FeignConfig;
import com.library.module.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "coupon-service",
        configuration = FeignConfig.class,
        fallback = CouponServiceClientFallback.class
)
public interface CouponServiceClient {

    @GetMapping("/api/v1/internal/coupons")
    ApiResponse<List<UserCouponResponse>> getUserCoupons(@RequestParam UUID userId);
}
