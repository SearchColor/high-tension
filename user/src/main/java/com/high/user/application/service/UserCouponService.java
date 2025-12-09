package com.high.user.application.service;

import com.high.user.infrastructure.client.CouponServiceClient;
import com.high.user.infrastructure.client.dto.UserCouponResponse;
import com.library.module.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final CouponServiceClient couponServiceClient;

    /**
     * 사용자의 보유 쿠폰 목록 조회
     *
     * @param userId 사용자 ID
     * @return 보유 쿠폰 목록
     */
    public List<UserCouponResponse> getUserCoupons(UUID userId) {
        log.info("Fetching user coupons: userId={}", userId);

        ApiResponse<List<UserCouponResponse>> response = couponServiceClient.getUserCoupons(userId);

        log.info("User coupons fetched: userId={}, count={}",
                userId,
                response.data() != null ? response.data().size() : 0);

        return response.data();
    }
}
