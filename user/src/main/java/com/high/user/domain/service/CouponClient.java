package com.high.user.domain.service;

import com.high.user.application.dto.response.CouponResponse;

import java.util.List;
import java.util.UUID;

/**
 * 쿠폰 서비스 클라이언트 인터페이스
 * DIP(의존성 역전 원칙)를 적용하여 Application 레이어가 구체적인 Feign 구현에 의존하지 않도록 함
 *
 * Infrastructure 계층에서 이 인터페이스를 구현하여 실제 Coupon Service와 통신
 */
public interface CouponClient {

    /**
     * 사용자의 보유 쿠폰 목록 조회
     *
     * @param userId 사용자 ID
     * @return 보유 쿠폰 목록 (실패 시 빈 리스트 반환)
     */
    List<CouponResponse> getUserCoupons(UUID userId);
}