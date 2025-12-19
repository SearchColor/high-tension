package com.high.orchestration.monitoring.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepository {
    Outbox save(Outbox message);

    List<Outbox> findRetryTargetsByTopic(String topic, LocalDateTime now );

    void flush();

    Optional<Outbox> findById(UUID dlqId);
}
