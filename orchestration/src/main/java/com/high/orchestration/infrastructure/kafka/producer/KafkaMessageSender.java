package com.high.orchestration.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.infrastructure.exception.MessagePublishFailedException;
import com.high.orchestration.infrastructure.exception.MessagePublishInterruptedException;
import com.high.orchestration.infrastructure.exception.MessageSerializationException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Retryable(
        retryFor = { MessagePublishFailedException.class },
        noRetryFor = {
            MessagePublishInterruptedException.class,
            MessageSerializationException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 300, multiplier = 2)
    )
    public void send(String topic, Object messageObj) {
        try {
            String json = toJson(messageObj);
            kafkaTemplate.send(topic, json).get();
            log.info("[KafkaEventPublisher] topic={}, message={}", topic, json);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagePublishInterruptedException(e);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof KafkaException
                || cause instanceof org.apache.kafka.common.errors.TimeoutException) {
                throw new MessagePublishFailedException(cause);
            }

            throw new MessageSerializationException(cause);
        }
    }

    private String toJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            if (json == null || json.isEmpty()) {
                throw new RuntimeException("[KafkaEventPublisher] Kafka 메시지 직렬화 실패: 빈 데이터");
            }
            return json;
        } catch (Exception e) {
            log.error("[KafkaEventPublisher] Kafka 메시지 JSON 변환 실패: {}", obj, e);
            throw new RuntimeException("Kafka 메시지 직렬화 오류", e);
        }
    }
}
