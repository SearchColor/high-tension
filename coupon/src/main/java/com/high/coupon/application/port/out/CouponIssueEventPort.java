package com.high.coupon.application.port.out;

import com.high.coupon.application.port.out.dto.CouponIssueCreateMessage;

/**
 * 쿠폰 발급 이벤트 발행 통로
 */
public interface CouponIssueEventPort {
    void publishIssueRequest(CouponIssueCreateMessage message);
}
