package com.high.orchestration.application.dto.internal.request;

import java.util.UUID;

public record ProcessOrderSuccessCommandRequest(
    UUID sagaId,
    UUID orderId
) {

}
