package com.high.orchestration.monitoring.domain;

import com.library.jpa.common.entity.BaseUpdateEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "p_kafka_dlq_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class KafkaDlqMessage extends BaseUpdateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID dlqId;

    private String originalTopic;
    private String dlqTopic;
    private Integer partitionNo; //이후 확장용
    private Long offsetNo;
    private String consumerGroup;

    private String payload;

    private String exceptionType;
    private String exceptionMessage;
    private String stackTrace;

    private int deliveryAttempt; //컨슈머에게 전달된 횟수(kafka retry횟수)
    private Integer retryCount =0; //dlq retry횟수
    private LocalDateTime nextRetryAt;



    @Enumerated(EnumType.STRING)
    private DlqStatus dlqStatus;

    private boolean isReprocessed = false;

    private LocalDateTime failedAt;


    private KafkaDlqMessage(
        String originalTopic,
        String dlqTopic,
        int partitionNo,
        Long offsetNo,
        String consumerGroup,

        String payload,

        String exceptionType,
        String exceptionMessage,
        String stackTrace,

        Integer deliveryAttempt,
        int retryCount,
        LocalDateTime nextRetryAt,

        DlqStatus dlqStatus
    ) {
        this.originalTopic = originalTopic;
        this.dlqTopic = dlqTopic;
        this.partitionNo = partitionNo;
        this.offsetNo = offsetNo;
        this.consumerGroup = consumerGroup;
        this.payload = payload;
        this.exceptionType = exceptionType;
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
        this.deliveryAttempt = deliveryAttempt;
        this.retryCount = retryCount;
        this.nextRetryAt = nextRetryAt;
        this.dlqStatus = dlqStatus;
    }

    public static KafkaDlqMessage create(
        String originalTopic,
        String dlqTopic,
        int partitionNo,
        Long offsetNo,
        String consumerGroup,

        String payload,

        String exceptionType,
        String exceptionMessage,
        String stackTrace,

        Integer deliveryAttempt,
        LocalDateTime nextRetryAt
        ) {
        return new KafkaDlqMessage(
            originalTopic,
            dlqTopic,
            partitionNo,
            offsetNo,
            consumerGroup,

            payload,

            exceptionType,
            exceptionMessage,
            stackTrace,
            deliveryAttempt,
            0,
            nextRetryAt,

            DlqStatus.RETRY_WAITING);
    }

    public void increaseRetry(LocalDateTime nextRetryAt) {
        this.retryCount++;
        this.nextRetryAt = nextRetryAt;
        this.dlqStatus = DlqStatus.RETRY_WAITING;
        log.info("[DLQ] 재시도 횟수 증가 - dlqId={}, retryCount={}, nextRetryAt={}",
            this.dlqId, this.retryCount, this.nextRetryAt);
    }

    public void markReprocessed() {
        this.dlqStatus = DlqStatus.RETRY_SUCCESS;
        this.isReprocessed = true;
        log.info("[DLQ] 재처리 성공 - dlqId={}, status={}", this.dlqId, this.dlqStatus);
    }

    public void markPermanentFail() {
        this.dlqStatus = DlqStatus.PERMANENT_FAIL;
        log.info("[DLQ] 영구 실패 처리 - dlqId={}, retryCount={}, status={}",
            this.dlqId, this.retryCount, this.dlqStatus);
    }

    //중복 처리 방지 위해
    public void markRetrying() {
        this.dlqStatus = DlqStatus.RETRYING;
        log.info("[DLQ] 재시도 진행 중 - dlqId={}, status={}", this.dlqId, this.dlqStatus);
    }
}
