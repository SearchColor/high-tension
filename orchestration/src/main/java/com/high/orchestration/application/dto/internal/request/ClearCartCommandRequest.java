package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record ClearCartCommandRequest(
    UUID sagaId,
    UUID orderId
) {

}
