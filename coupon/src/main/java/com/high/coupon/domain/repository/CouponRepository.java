package com.high.coupon.domain.repository;

import com.high.coupon.domain.entity.Coupon;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository {

    Coupon save(Coupon coupon);
    Optional<Coupon> findById(UUID id);
    List<Coupon> findAll();
}
