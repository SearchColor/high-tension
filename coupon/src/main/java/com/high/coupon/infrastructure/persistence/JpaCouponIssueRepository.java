package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.CouponIssue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaCouponIssueRepository extends JpaRepository<CouponIssue, UUID> {

    // 리스트 조회
    @Query("""
    SELECT ci FROM CouponIssue ci
    JOIN FETCH ci.coupon
    WHERE ci.userId = :userId
      AND ci.isUsed = false
      AND ci.validStartAt <= CURRENT_TIMESTAMP
      AND ci.validEndAt >= CURRENT_TIMESTAMP
""")
    List<CouponIssue> findAvailableByUserId(@Param("userId") UUID userId);

    // 단건 검증 조회용
    @Query("""
        SELECT ci FROM CouponIssue ci
        JOIN FETCH ci.coupon
        WHERE ci.id = :couponIssueId
          AND ci.userId = :userId
    """)
    Optional<CouponIssue> findByIdAndUserId(
            @Param("couponIssueId") UUID couponIssueId,
            @Param("userId") UUID userId
    );

    // 쿠폰 사용 Atomic
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE CouponIssue c
        SET c.isUsed = true,
            c.usedAt = :usedAt,
            c.updatedAt = :usedAt,
            c.updatedBy = :userId
        WHERE c.id = :couponIssueId
          AND c.userId = :userId
          AND c.isUsed = false
    """)
    int useCouponIfAvailable(
            UUID couponIssueId,
            UUID userId,
            LocalDateTime usedAt
    );
}
