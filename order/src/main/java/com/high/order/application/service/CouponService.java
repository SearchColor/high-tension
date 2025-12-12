package com.high.order.application.service;

import com.high.order.application.dto.external.CouponResponse;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;

public interface CouponService {
    ApiResponse<CouponResponse> validateCoupon(@PathVariable UUID couponIssueId);

}
