package com.high.order.infrastructure.client;


import com.high.order.application.dto.external.CouponResponse;
import com.high.order.application.service.CouponService;
import com.library.module.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "coupon-service")
public interface CouponClient extends CouponService {
    //임시
    @GetMapping
    ResponseEntity<ApiResponse<CouponResponse>> coupon();

}
