package com.high.coupon.domain.repository.redis;

import java.util.UUID;

/**
 * Redis Lua 인터페이스
 */
public interface CouponRedisRepository {
    Long tryIssueCoupon(UUID couponId, UUID userId, Integer totalQuantity);
}
