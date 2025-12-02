package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.CouponIssue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCouponIssueRepository extends JpaRepository<CouponIssue, UUID> {

    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);
    long countByCouponId(UUID couponId);
}
