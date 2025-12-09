package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record CouponRestoreCommandRequest(
    UUID sagaId,
    UUID orderId,
    UUID userId
) {

}
