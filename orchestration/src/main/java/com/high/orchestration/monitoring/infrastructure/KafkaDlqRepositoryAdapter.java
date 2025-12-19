package com.high.orchestration.monitoring.infrastructure;

import com.high.orchestration.monitoring.domain.KafkaDlqMessage;
import com.high.orchestration.monitoring.domain.KafkaDlqRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDlqRepositoryAdapter implements KafkaDlqRepository {

    @Override
    public Optional<KafkaDlqMessage> findById(UUID dlqId) {
        return kafkaDlqJpaRepository.findById(dlqId);
    }

    private final KafkaDlqJpaRepository kafkaDlqJpaRepository;

    @Override
    public KafkaDlqMessage save(KafkaDlqMessage message) {
        return kafkaDlqJpaRepository.save(message);
    }

    @Override
    public List<KafkaDlqMessage> findRetryTargetsByTopic(String topic, LocalDateTime now) {
        return kafkaDlqJpaRepository.findRetryTargetsByTopic(topic, now);
    }

    @Override
    public void flush() {
        kafkaDlqJpaRepository.flush();
    }
}
