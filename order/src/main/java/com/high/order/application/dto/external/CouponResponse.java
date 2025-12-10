package com.high.order.application.dto.external;

import java.math.BigDecimal;
import java.util.UUID;

public record CouponResponse(
    UUID couponIssueId,
    UUID couponId,
    String couponName,
    BigDecimal discountRate
) {

}
