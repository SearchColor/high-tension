package com.high.coupon.infrastructure.persistence;

import com.high.coupon.domain.entity.Coupon;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCouponRepository extends JpaRepository<Coupon, UUID> {

}
