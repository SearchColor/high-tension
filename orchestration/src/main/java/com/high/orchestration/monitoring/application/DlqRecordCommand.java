package com.high.orchestration.monitoring.application;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public record DlqRecordCommand(
        String originalTopic,
        String dlqTopic,
        int partition,
        Long offset,
        String consumerGroup,

        String messageKey,
        String payload,

        String exceptionType,
        String exceptionMessage,
        String stackTrace,

        int deliveryAttempt
) {

    public static DlqRecordCommand from(
        ConsumerRecord<String, String> record,
        String consumerGroup,
        Integer deliveryAttempt,
        String exceptionType,
        String exceptionMessage,
        byte[] stackTraceBytes
    ) {
        return new DlqRecordCommand(
            extractOriginalTopic(record.topic()),
            record.topic(),
            record.partition(),
            record.offset(),
            consumerGroup,
            record.key(),
            record.value(),
            exceptionType,
            exceptionMessage,
            stackTraceBytes != null
            ? new String(stackTraceBytes, StandardCharsets.UTF_8)
                : null,
            deliveryAttempt != null? deliveryAttempt : 1
            );
    }

    private static String extractOriginalTopic(String dlqTopic) {
        return dlqTopic.endsWith("-dlq")
            ? dlqTopic.substring(0, dlqTopic.length() - 4)
            : dlqTopic;
    }

}
