package com.high.orchestration.application.dto.internal.response;

import java.util.UUID;

public record OrderCreateFailCommandResponse(
    UUID sagaId,
    String reason,
    UUID userId
) {

}
