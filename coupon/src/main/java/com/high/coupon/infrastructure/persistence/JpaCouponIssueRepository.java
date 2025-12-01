package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.CouponIssue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCouponIssueRepository extends JpaRepository<CouponIssue, UUID> {

    // todo: 쿠폰 생성 - 조회 기본 구현 완료 후 발급 이력 개발
}
