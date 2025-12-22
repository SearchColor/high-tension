package com.high.coupon.application.port.out;

import java.util.UUID;

/**
 * Redis 관련 쿠폰 통로 역할
 */
public interface CouponCachePort {
    Long tryIssueCoupon(UUID couponId, UUID userId, Integer totalQuantity, Long ttlSeconds);
}
