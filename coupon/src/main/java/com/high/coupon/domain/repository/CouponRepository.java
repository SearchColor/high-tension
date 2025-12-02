package com.high.coupon.domain.repository;

import com.high.coupon.domain.entity.Coupon;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponRepository {

    Coupon save(Coupon coupon);
    Optional<Coupon> findById(UUID id);
    Page<Coupon> findAll(Pageable pageable);
}
