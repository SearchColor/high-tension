package com.high.orchestration.monitoring.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DlqPermanentFailedMessage(
    UUID dlqId,
    String originalTopic,
    String payload,
    String exceptionType,
    String exceptionMessage,
    LocalDateTime failedAt
) {
    public static DlqPermanentFailedMessage of(
        UUID dlqId,
        String originalTopic,
        String payload,
        String exceptionType,
        String exceptionMessage) {
        return new DlqPermanentFailedMessage(
            dlqId,
            originalTopic,
            payload,
            exceptionType,
            exceptionMessage,
            LocalDateTime.now()
        );
    }

}
