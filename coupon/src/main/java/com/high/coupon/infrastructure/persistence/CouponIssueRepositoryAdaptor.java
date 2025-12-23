package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.CouponIssue;
import com.high.coupon.domain.repository.CouponIssueRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepositoryAdaptor implements CouponIssueRepository {

    private final JpaCouponIssueRepository jpaCouponIssueRepository;

    @Override
    public void save(CouponIssue couponIssue) {
        jpaCouponIssueRepository.save(couponIssue);
    }

    @Override
    public Optional<CouponIssue> findById(UUID couponIssueId) {
        return jpaCouponIssueRepository.findById(couponIssueId);
    }

    @Override
    public List<CouponIssue> findAvailableByUserId(UUID userId){
        return jpaCouponIssueRepository.findAvailableByUserId(userId);
    }

    @Override
    public Optional<CouponIssue> findByIdAndUserId(UUID couponIssueId, UUID userId){
        return jpaCouponIssueRepository.findByIdAndUserId(couponIssueId, userId);
    }

    @Override
    public int useCouponIfAvailable(UUID couponIssueId, UUID userId, LocalDateTime usedAt) {
        return jpaCouponIssueRepository.useCouponIfAvailable(couponIssueId, userId, usedAt);
    }
}
