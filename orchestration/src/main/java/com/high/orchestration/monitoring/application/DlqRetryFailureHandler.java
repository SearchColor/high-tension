package com.high.orchestration.monitoring.application;

import com.high.orchestration.monitoring.application.dto.DlqPermanentFailedMessage;
import com.high.orchestration.monitoring.domain.Outbox;
import com.high.orchestration.monitoring.domain.OutboxRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqRetryFailureHandler {

    private final OutboxRepository outboxRepository;
    private final DlqEventPublisher dlqEventPublisher;

    @Transactional
    public void handleRetryFailure(String outboxId, Exception exception) {
        log.error(
            "[DLQ Retry Failure] DLQ 재시도 메시지 최종 실패 - outboxId={}, error={}",
            outboxId,
            exception.getMessage()
        );

        try {
            Outbox message = outboxRepository.findById(UUID.fromString(outboxId))
                .orElseThrow(() -> new IllegalArgumentException("DLQ 메시지를 찾을 수 없음: " + outboxId));

            message.markPermanentFail();
            outboxRepository.save(message);

            log.info(
                "[DLQ Retry Failure] 영구 실패 처리 완료 - outboxId={}, retryCount={}",
                outboxId,
                message.getRetryCount()
            );

            // 영구 실패 이벤트 발행
            publishPermanentFailed(message);

        } catch (Exception e) {
            log.error("[DLQ Retry Failure] 영구 실패 처리 중 오류 - outboxId={}", outboxId, e);
            // TODO: Slack 알림
        }
    }

    private void publishPermanentFailed(Outbox message) {
        try {
            DlqPermanentFailedMessage failedMessage = DlqPermanentFailedMessage.of(
                message.getOutboxId(),
                message.getOriginalTopic(),
                message.getPayload(),
                message.getExceptionType(),
                message.getExceptionMessage()
            );

            dlqEventPublisher.publishPermanentFailed("dlq-permanent-failed", failedMessage);

            log.info(
                "[DLQ Retry Failure] 영구 실패 이벤트 발행 완료 - outboxId={}",
                message.getOutboxId()
            );

        } catch (Exception e) {
            log.error(
                "[DLQ Retry Failure] 영구 실패 이벤트 발행 실패 - outboxId={}",
                message.getOutboxId(),
                e
            );
        }
    }
}
