package com.high.orchestration.monitoring.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KafkaDlqRepository {
    KafkaDlqMessage save(KafkaDlqMessage message);

    List<KafkaDlqMessage> findRetryTargetsByTopic(String topic, LocalDateTime now );

    void flush();

    Optional<KafkaDlqMessage> findById(UUID dlqId);
}
