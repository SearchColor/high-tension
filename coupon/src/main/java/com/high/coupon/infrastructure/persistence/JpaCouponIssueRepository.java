package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.CouponIssue;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaCouponIssueRepository extends JpaRepository<CouponIssue, UUID> {

    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);
    long countByCouponId(UUID couponId);

    @Query("""
    SELECT ci FROM CouponIssue ci
    JOIN FETCH ci.coupon
    WHERE ci.userId = :userId
      AND ci.isUsed = false
      AND ci.validStartAt <= CURRENT_TIMESTAMP
      AND ci.validEndAt >= CURRENT_TIMESTAMP
""")
    List<CouponIssue> findAvailableByUserId(@Param("userId") UUID userId);
}
