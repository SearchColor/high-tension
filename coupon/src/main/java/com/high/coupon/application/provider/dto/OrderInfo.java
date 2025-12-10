package com.high.coupon.application.provider.dto;

import java.util.UUID;

public record OrderInfo(
        UUID orderId,
        UUID userId,
        UUID couponIssueId
) {
}
