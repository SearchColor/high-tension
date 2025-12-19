package com.high.coupon.application;

import com.high.coupon.application.exception.CouponNotFoundException;
import com.high.coupon.domain.entity.Coupon;
import com.high.coupon.domain.repository.CouponRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponReader {

    private final CouponRepository couponRepository;

    /**
     * 쿠폰 ID 조회 메서드
     */
    @Cacheable(cacheNames = "coupon", key = "#couponId", cacheManager = "localCacheManager", sync = true)
    public Coupon getCouponById(UUID couponId){

        log.info("[CouponReader Cache] 쿠폰 조회 id={}", couponId);

        return couponRepository.findById(couponId)
                .orElseThrow(CouponNotFoundException::new);
    }
}
