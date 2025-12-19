package com.high.coupon.infrastructure.persistence.redis;

import com.high.coupon.application.port.out.CouponCachePort;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

/**
 * 쿠폰 발급을 위한 Redis Repo 구현체
 * Lua Script를 사용해 발급 시도 과정(중복, 수량 확인)을 단일 연산으로 처리
 */
@Repository
@RequiredArgsConstructor
public class CouponCacheRedisRepositoryImpl implements CouponCachePort {

    private final StringRedisTemplate redisTemplate;

    // RedisConfig에서 등록한 Bean 이름과 일치해야 함
    private final RedisScript<Long> issueCouponScript;

    @Override
    public Long tryIssueCoupon(UUID couponId, UUID userId, Integer totalQuantity) {
        String key = "coupon:" + couponId + ":users";

        return redisTemplate.execute(
                issueCouponScript,
                Collections.singletonList(key),
                userId.toString(),
                String.valueOf(totalQuantity)
        );
    }
}
