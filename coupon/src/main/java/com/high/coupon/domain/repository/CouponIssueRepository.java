package com.high.coupon.domain.repository;

import com.high.coupon.domain.entity.CouponIssue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponIssueRepository {

    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);
    long countByCouponId(UUID couponId);
    CouponIssue save(CouponIssue couponIssue);
    Optional<CouponIssue> findById(UUID couponIssueId);

    // 전체 조회 todo: 관리자의 사용자 별 쿠폰 조회(사용 만료, 사용 가능)가 필요한가? - 주석처리
    // List<CouponIssue> findAllByUserId(UUID userId);

    // 사용자 별 사용 가능 쿠폰만 조회
    List<CouponIssue> findAvailableByUserId(UUID userId);
}
