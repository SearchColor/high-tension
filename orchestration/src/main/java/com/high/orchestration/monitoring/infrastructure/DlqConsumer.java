package com.high.orchestration.monitoring.infrastructure;

import com.high.orchestration.monitoring.application.DlqRecordCommand;
import com.high.orchestration.monitoring.application.DlqRecordService;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

        @Header(name = "retry_topic-attempts", required = false)
        byte[] attemptsBytes,

        @Header(name = "kafka_exception-fqcn", required = false)
        byte[] exceptionTypeBytes,

        @Header(name = "kafka_exception-message", required = false)
        byte[] exceptionMessageBytes,

        @Header(name = "kafka_exception-stacktrace", required = false)
        byte[] exceptionStackTraceBytes
    ) {

        Integer attempts = null;
        String exceptionType = null;
        String exceptionMessage = null;
        String exceptionStackTrace = null;

        if (attemptsBytes != null) {
            attempts = ByteBuffer.wrap(attemptsBytes).getInt();
        }

        if (exceptionTypeBytes != null) {
            exceptionType = new String(exceptionTypeBytes, StandardCharsets.UTF_8);
        }

        if (exceptionMessageBytes != null) {
            exceptionMessage = new String(exceptionMessageBytes, StandardCharsets.UTF_8);
        }

        if (exceptionStackTraceBytes != null) {
            exceptionStackTrace = new String(exceptionMessageBytes, StandardCharsets.UTF_8);
        }


        log.error(
            "[DLQ] topic={}, partition={}, offset={}, exceptionType={}, message={}, attempt={}",
            record.topic(),
            record.partition(),
            record.offset(),
            exceptionType,
            exceptionMessage,
            attempts
        );

        try {
            DlqRecordCommand command = DlqRecordCommand.from(
                record,
                consumerGroup,
                attempts,
                exceptionType,
                exceptionMessage,
                exceptionStackTrace
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
