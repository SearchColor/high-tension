package com.high.coupon.domain.repository;

import com.high.coupon.domain.entity.CouponIssue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponIssueRepository {

    CouponIssue save(CouponIssue couponIssue);
    Optional<CouponIssue> findById(UUID couponIssueId);

    // 사용자 별 사용 가능 쿠폰만 조회
    List<CouponIssue> findAvailableByUserId(UUID userId);

    // 쿠폰 단건 검증
    Optional<CouponIssue> findByIdAndUserId(UUID couponIssueId, UUID userId);
}
