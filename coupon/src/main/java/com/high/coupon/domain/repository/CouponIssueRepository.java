package com.high.coupon.domain.repository;

import com.high.coupon.domain.entity.CouponIssue;
import java.util.Optional;
import java.util.UUID;

public interface CouponIssueRepository {

    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);
    long countByCouponId(UUID couponId);
    CouponIssue save(CouponIssue couponIssue);
    Optional<CouponIssue> findById(UUID couponIssueId);
}
