package com.high.coupon.infrastructure.client.dto;

import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID userId,
        UUID couponIssueId
) {
}
