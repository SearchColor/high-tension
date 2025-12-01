package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponRepositoryAdaptor implements CouponRepository {

    // private final JpaCouponRepository jpaCouponRepository;

}