package com.high.orchestration.monitoring.application;

import com.high.orchestration.monitoring.application.dto.DlqPermanentFailedMessage;
import com.high.orchestration.monitoring.domain.KafkaDlqMessage;
import com.high.orchestration.monitoring.domain.KafkaDlqRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqRetryFailureHandler {

    private final KafkaDlqRepository kafkaDlqRepository;
    private final DlqEventPublisher dlqEventPublisher;

    @Transactional
    public void handleRetryFailure(String dlqId, Exception exception) {
        log.error(
            "[DLQ Retry Failure] DLQ 재시도 메시지 최종 실패 - dlqId={}, error={}",
            dlqId,
            exception.getMessage()
        );

        try {
            KafkaDlqMessage message = kafkaDlqRepository.findById(UUID.fromString(dlqId))
                .orElseThrow(() -> new IllegalArgumentException("DLQ 메시지를 찾을 수 없음: " + dlqId));

            message.markPermanentFail();
            kafkaDlqRepository.save(message);

            log.info(
                "[DLQ Retry Failure] 영구 실패 처리 완료 - dlqId={}, retryCount={}",
                dlqId,
                message.getRetryCount()
            );

            // 영구 실패 이벤트 발행
            publishPermanentFailed(message);

        } catch (Exception e) {
            log.error("[DLQ Retry Failure] 영구 실패 처리 중 오류 - dlqId={}", dlqId, e);
            // TODO: Slack 알림
        }
    }

    private void publishPermanentFailed(KafkaDlqMessage message) {
        try {
            DlqPermanentFailedMessage failedMessage = DlqPermanentFailedMessage.of(
                message.getDlqId(),
                message.getOriginalTopic(),
                message.getPayload(),
                message.getExceptionType(),
                message.getExceptionMessage()
            );

            dlqEventPublisher.publishPermanentFailed("dlq-permanent-failed", failedMessage);

            log.info(
                "[DLQ Retry Failure] 영구 실패 이벤트 발행 완료 - dlqId={}",
                message.getDlqId()
            );

        } catch (Exception e) {
            log.error(
                "[DLQ Retry Failure] 영구 실패 이벤트 발행 실패 - dlqId={}",
                message.getDlqId(),
                e
            );
        }
    }
}
