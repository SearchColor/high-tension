package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.CouponIssue;
import com.high.coupon.domain.repository.CouponIssueRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepositoryAdaptor implements CouponIssueRepository {

    private final JpaCouponIssueRepository jpaCouponIssueRepository;

    @Override
    public boolean existsByCouponIdAndUserId(UUID couponId, UUID userId) {
        return jpaCouponIssueRepository.existsByCouponIdAndUserId(couponId, userId);
    }

    @Override
    public long countByCouponId(UUID couponId) {
        return jpaCouponIssueRepository.countByCouponId(couponId);
    }

    @Override
    public CouponIssue save(CouponIssue couponIssue) {
        return jpaCouponIssueRepository.save(couponIssue);
    }

    @Override
    public Optional<CouponIssue> findById(UUID couponIssueId) {
        return jpaCouponIssueRepository.findById(couponIssueId);
    }
}
