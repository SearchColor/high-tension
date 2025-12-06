package com.high.order.infrastructure.client;


import com.high.order.application.service.CouponService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "coupon-service")
public interface CouponClient extends CouponService {

}
