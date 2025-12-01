package com.high.coupon.presentation;

import com.high.coupon.application.CouponService;
import com.high.coupon.application.dto.request.CouponCreateRequest;
import com.high.coupon.application.dto.response.CouponCreateResponse;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    // todo 권한 필요

    // 쿠폰 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CouponCreateResponse>> createCoupon(
            @RequestBody @Valid CouponCreateRequest request){
        var response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
