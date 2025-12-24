package com.high.orchestration.monitoring.application;

import com.high.orchestration.monitoring.application.dto.DlqPermanentFailedMessage;
import com.high.orchestration.monitoring.domain.Outbox;
import com.high.orchestration.monitoring.domain.OutboxRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreateSuccessDlqRetryScheduler {

    private static final int MAX_RETRY = 3;
    private static final String TOPIC = "order-create-success";

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DlqEventPublisher dlqEventPublisher;

    @Scheduled(fixedDelay = 60000) //10초 (로그 확인이 어려워 임시로 60초 설정)
    public void retryDlqMessages() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        log.info("[DLQ Scheduler] 재시도 프로세스 시작 - 현재시간={}", now);

        log.info("[DLQ Scheduler] 재시도 대상 메시지 조회 시작");

        List<Outbox> targets = outboxRepository.findRetryTargetsByTopic(TOPIC, now);

        if (targets.isEmpty()) {
            log.info("[DLQ Scheduler] 재시도 대상 메시지 없음");
            return;
        }

        log.info("[DLQ Scheduler] 재시도 대상 메시지 {}개 발견", targets.size());

        int successCount = 0;
        int failCount = 0;
        int permanentFailCount = 0;

        for (Outbox message : targets) {
            try {
                RetryResult result = retryMessage(message);

                switch (result) {
                    case SUCCESS -> successCount++;
                    case FAILED -> failCount++;
                    case PERMANENT_FAIL -> permanentFailCount++;
                }

            } catch (Exception e) {
                failCount++;
                log.error("[DLQ Scheduler] 메시지 처리 중 예외 - outboxId={}",
                    message.getOutboxId(), e);
            }
        }

        log.info(
            "[DLQ Scheduler] DLQ -> Consumer 재시도 요청 완료 - 성공: {}, 재시도 필요: {}, 영구실패: {}",
            successCount, failCount, permanentFailCount
        );
    }



    @Transactional
    protected RetryResult retryMessage(Outbox message) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);


        log.info(
            "[DLQ Retry] 메시지 재시도 시작 - outboxId={}, retryCount={}, nextRetryAt={}, 현재시간={}",
            message.getOutboxId(),
            message.getRetryCount(),
            message.getNextRetryAt(),
            now        );

        // 최대 재시도 횟수 초과 체크
        if (message.getRetryCount() >= MAX_RETRY) {
            log.warn("[DLQ Retry] 최대 재시도 횟수 초과 - outboxId={}, retryCount={}",
                message.getOutboxId(), message.getRetryCount());

            message.markPermanentFail();
            outboxRepository.save(message);

            // 영구 실패 이벤트 발행
            publishPermanentFailed(message);
            return RetryResult.PERMANENT_FAIL;
        }



        // 재시도 진행 중으로 상태 변경 (중복 처리 방지)
        message.markRetrying();
        outboxRepository.save(message);
        outboxRepository.flush();  // 즉시 DB 반영

        try {
            log.info("[DLQ Retry] Kafka 재전송 시도 - outboxId={}, topic={}",
                message.getOutboxId(), message.getOriginalTopic());

            //헤더에 dlq에서 출발하는 메시지임을 담음
            Message<String> kafkaMessage = MessageBuilder
                .withPayload(message.getPayload())
                .setHeader(KafkaHeaders.TOPIC, message.getOriginalTopic())
                .setHeader("from-dlq", true)
                .setHeader("outboxId", message.getOutboxId().toString())
                .build();

            kafkaTemplate.send(kafkaMessage).get(5, TimeUnit.SECONDS); //동기호출 (timeout 5초)

            log.info("[DLQ Retry] 재시도 성공 - outboxId={}, retryCount={}",
                message.getOutboxId(), message.getRetryCount());

            message.markReprocessed();
            outboxRepository.save(message);
            return RetryResult.SUCCESS;

        } catch (Exception e) {
            log.warn("[DLQ Retry] 재시도 실패 - outboxId={}, retryCount={}, nextRetry={}",
                message.getOutboxId(),
                message.getRetryCount(),
                LocalDateTime.now(ZoneOffset.UTC).plusMinutes(2),
                e);

            // 다음 재시도 시간 계산 (지수 백오프)
            int nextDelayMinutes = (int) Math.pow(2, message.getRetryCount()) * 2;
            LocalDateTime nextRetryAt = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(nextDelayMinutes);

            message.increaseRetry(nextRetryAt);
            outboxRepository.save(message);

            log.info(
                "[DLQ Retry] 다음 재시도 예정 - outboxId={}, nextRetryAt={}, delayMinutes={}",
                message.getOutboxId(),
                nextRetryAt,
                nextDelayMinutes
            );

            return RetryResult.FAILED;
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

            log.info("[DLQ Retry] 영구 실패 이벤트 발행 완료 - outboxId={}", message.getOutboxId());

        } catch (Exception e) {
            log.error("[DLQ Retry] 영구 실패 이벤트 발행 실패 - outboxId={}", message.getOutboxId(), e);
        }
    }


    private enum RetryResult {
        SUCCESS,           // 재시도 성공
        FAILED,           // 재시도 실패 (다시 시도 예정)
        PERMANENT_FAIL  // 영구 실패
    }
}
