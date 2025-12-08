package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record CouponUseCommandRequest(
    UUID sagaId,
    UUID orderId
) {

}
