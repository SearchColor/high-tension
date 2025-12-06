package com.high.order.application.service;

import com.high.order.application.dto.external.CouponResponse;
import com.library.module.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface CouponService {
    //임시
    ResponseEntity<ApiResponse<CouponResponse>> coupon();

}
