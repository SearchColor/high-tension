package com.high.orchestration.monitoring.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DlqPermanentFailedMessage(
    UUID outboxId,
    String originalTopic,
    String payload,
    String exceptionType,
    String exceptionMessage,
    LocalDateTime failedAt
) {
    public static DlqPermanentFailedMessage of(
        UUID outboxId,
        String originalTopic,
        String payload,
        String exceptionType,
        String exceptionMessage) {
        return new DlqPermanentFailedMessage(
            outboxId,
            originalTopic,
            payload,
            exceptionType,
            exceptionMessage,
            LocalDateTime.now()
        );
    }

}
