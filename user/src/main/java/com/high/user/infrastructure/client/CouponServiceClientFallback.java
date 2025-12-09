package com.high.user.infrastructure.client;

import com.high.user.infrastructure.client.dto.UserCouponResponse;
import com.library.module.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CouponServiceClientFallback implements CouponServiceClient {

    @Override
    public ApiResponse<List<UserCouponResponse>> getUserCoupons(UUID userId) {
        log.warn("Coupon Service unavailable. Returning empty list for userId={}", userId);
        return ApiResponse.success(Collections.emptyList());
    }
}
