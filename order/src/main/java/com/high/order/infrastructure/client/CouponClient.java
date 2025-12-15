package com.high.order.infrastructure.client;


import com.high.order.application.dto.external.CouponResponse;
import com.high.order.application.service.CouponService;
import com.high.order.infrastructure.config.feign.FeignConfig;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "coupon-service", configuration = FeignConfig.class)
public interface CouponClient extends CouponService {
    //임시
    @GetMapping("/api/v1/internal/coupons/{couponIssueId}/validate")
    ApiResponse<CouponResponse> validateCoupon(@PathVariable UUID couponIssueId);

}
