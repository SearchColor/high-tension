package com.high.orchestration.monitoring.infrastructure;

import com.high.orchestration.monitoring.application.DlqRecordCommand;
import com.high.orchestration.monitoring.application.DlqRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class DlqConsumer {

    private final DlqRecordService dlqRecordService;
    //private final KafkaDlqEventPublisher publisher;

    //private final OrderCreateSuccessDlqRetryScheduler dlqRetryService;

    @KafkaListener(topics = "order-create-success-dlq")
    public void orderCreateSuccessDlq(
        ConsumerRecord<String, String> record,

        @Header(name = KafkaHeaders.GROUP_ID, required = false)
        String consumerGroup,

        @Header(name = KafkaHeaders.DELIVERY_ATTEMPT, required = false)
        Integer attempt,

        @Header(name = KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN, required = false)
        String exceptionType,

        @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false)
        String exceptionMessage,

        @Header(name = KafkaHeaders.DLT_EXCEPTION_STACKTRACE, required = false)
        byte[] stackTrace
    ) {
        //int safeAttempt = attempt != null ? attempt : 0;

        //log.error("[DLQ] order-create-success 실패 ");

        log.error(
            "[DLQ] topic={}, partition={}, offset={}, exceptionType={}, message={}, attempt={}",
            record.topic(),
            record.partition(),
            record.offset(),
            exceptionType,
            exceptionMessage,
            attempt
        );

        try {
            DlqRecordCommand command = DlqRecordCommand.from(
                record,
                consumerGroup,
                attempt,
                exceptionType,
                exceptionMessage,
                stackTrace
            );


            dlqRecordService.record(command);

            //publisher.publishPermanentFailed("dlq-fail-message", message);
            log.info("dlq 실패 이벤트 발행 성공");
            log.info(
                "[DLQ Consumer] 메시지 저장 완료 - topic={}, partition={}, offset={}",
                record.topic(),
                record.partition(),
                record.offset()
            );

        } catch (Exception e) {
            log.error("[DLQ] DLQ 메시지 저장 실패 - record={}", record, e);
            //TDOO: Dlq 도달 실패 slack alarm 처리

        }
    }



}
