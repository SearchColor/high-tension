package com.high.orchestration.monitoring.application;

import com.high.orchestration.monitoring.domain.Outbox;
import com.high.orchestration.monitoring.domain.OutboxRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqRecordService {
    private final OutboxRepository kafkaDlqRepository;


    public Outbox record(DlqRecordCommand command) {
        LocalDateTime nextRetryAt = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1);

        Outbox message = Outbox.create(
            command.originalTopic(),
            command.dlqTopic(),
            command.partition(),
            command.offset(),
            command.consumerGroup(),

            command.payload(),

            command.exceptionType(),
            command.exceptionMessage(),
            command.stackTrace(),

            command.deliveryAttempt(),
            nextRetryAt
        );

        return kafkaDlqRepository.save(message);

    }
}
