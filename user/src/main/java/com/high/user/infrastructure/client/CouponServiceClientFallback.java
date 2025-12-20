package com.high.user.infrastructure.client;

import com.high.user.infrastructure.client.dto.UserCouponResponse;
import com.library.module.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

/**
 * CouponServiceClient Fallback 구현체
 * Circuit Breaker가 OPEN 상태일 때 호출됩니다.
 */
@Slf4j
@Component
public class CouponServiceClientFallback implements CouponServiceClient {

    /**
     * Coupon Service 장애 시 Fallback 응답
     * Circuit Breaker OPEN 또는 Timeout 발생 시 빈 리스트 반환
     *
     * @return 빈 쿠폰 리스트
     */
    @Override
    public ApiResponse<List<UserCouponResponse>> getUserCoupons() {
        log.warn("Coupon Service fallback triggered");

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                attributes.getResponse().addHeader("X-Fallback-Triggered", "true");
            }
        } catch (Exception e) {
            log.warn("Failed to add fallback header to response", e);
        }

        return ApiResponse.success(Collections.emptyList());
    }
}
