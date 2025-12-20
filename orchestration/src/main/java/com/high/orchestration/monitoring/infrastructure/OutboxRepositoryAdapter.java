package com.high.orchestration.monitoring.infrastructure;

import com.high.orchestration.monitoring.domain.Outbox;
import com.high.orchestration.monitoring.domain.OutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    @Override
    public Optional<Outbox> findById(UUID dlqId) {
        return outboxJpaRepository.findById(dlqId);
    }

    @Override
    public Outbox save(Outbox message) {
        return outboxJpaRepository.save(message);
    }

    @Override
    public List<Outbox> findRetryTargetsByTopic(String topic, LocalDateTime now) {
        return outboxJpaRepository.findRetryTargetsByTopic(topic, now);
    }

    @Override
    public void flush() {
        outboxJpaRepository.flush();
    }
}
