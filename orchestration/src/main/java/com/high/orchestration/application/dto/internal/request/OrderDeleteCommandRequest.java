package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record OrderDeleteCommandRequest(
    UUID sagaId,
    UUID orderId
) {

}
