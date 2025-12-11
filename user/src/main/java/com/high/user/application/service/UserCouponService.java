package com.high.user.application.service;

import com.high.user.application.dto.response.CouponResponse;
import com.high.user.domain.service.CouponClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final CouponClient couponClient;

    /**
     * 사용자의 보유 쿠폰 목록 조회
     *
     * @param userId 사용자 ID
     * @return 보유 쿠폰 목록
     */
    public List<CouponResponse> getUserCoupons(UUID userId) {
        log.info("Fetching user coupons: userId={}", userId);

        List<CouponResponse> coupons = couponClient.getUserCoupons(userId);

        log.info("User coupons fetched: userId={}, count={}", userId, coupons.size());

        return coupons;
    }
}
