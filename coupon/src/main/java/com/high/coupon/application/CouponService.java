package com.high.coupon.application;

import com.high.coupon.application.dto.request.CouponCreateRequest;
import com.high.coupon.application.dto.response.CouponCreateResponse;
import com.high.coupon.domain.entity.Coupon;
import com.high.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    // todo : 권한 검증 필요

    // 쿠폰 등록
    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request){

        Coupon coupon = Coupon.createCoupon(
                request.name(),
                request.description(),
                request.discountRate(),
                request.totalQuantity(),
                request.issueStartAt(),
                request.issueEndAt(),
                request.validUntil()
        );

        Coupon savedCoupon = couponRepository.save(coupon);
        return CouponCreateResponse.from(savedCoupon);
    }
}
