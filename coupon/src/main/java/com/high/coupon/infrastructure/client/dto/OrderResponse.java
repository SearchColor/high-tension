package com.high.coupon.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(
        UUID orderId,
        UUID userId,
        UUID couponIssueId
) {
}
