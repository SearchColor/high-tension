package com.high.coupon.application.port.out.dto;

import java.util.UUID;

public record OrderInfo(
        UUID orderId,
        UUID userId,
        UUID couponIssueId
) {
}
