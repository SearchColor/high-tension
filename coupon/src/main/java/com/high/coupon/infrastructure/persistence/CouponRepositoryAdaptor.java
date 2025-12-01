package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.Coupon;
import com.high.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponRepositoryAdaptor implements CouponRepository {

    private final JpaCouponRepository jpaCouponRepository;

    @Override
    public Coupon save(Coupon coupon){
        return jpaCouponRepository.save(coupon);
    }

}