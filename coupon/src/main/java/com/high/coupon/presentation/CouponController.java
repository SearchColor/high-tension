package com.high.coupon.presentation;

import com.high.coupon.application.CouponService;
import com.high.coupon.application.dto.request.CouponCreateRequest;
import com.high.coupon.application.dto.response.CouponCreateResponse;
import com.high.coupon.application.dto.response.CouponDetailResponse;
import com.high.coupon.application.dto.response.CouponListResponse;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 발행 쿠폰 상세 조회 (관리자)
    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCouponById(
            @PathVariable("couponId") UUID couponId) {
        var response = couponService.getCouponDetail(couponId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 발행 쿠폰 리스트 조회 (관리자)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponListResponse>>> getAllCoupons() {
        var responseList = couponService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }
}
