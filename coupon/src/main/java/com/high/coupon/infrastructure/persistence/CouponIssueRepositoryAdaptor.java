package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.repository.CouponIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepositoryAdaptor implements CouponIssueRepository {

    // todo: 쿠폰 생성 - 조회 기본 구현 완료 후 발급 이력 개발
    private final JpaCouponIssueRepository jpaCouponIssueRepository;

}
