package com.high.orchestration.monitoring.application;

import com.high.orchestration.monitoring.domain.KafkaDlqMessage;
import com.high.orchestration.monitoring.domain.KafkaDlqRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqRecordService {
    private final KafkaDlqRepository kafkaDlqRepository;


    public KafkaDlqMessage record(DlqRecordCommand command) {
        LocalDateTime nextRetryAt = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1);

        KafkaDlqMessage message = KafkaDlqMessage.create(
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

        KafkaDlqMessage dlqMessage = kafkaDlqRepository.save(message);

//        DlqPermanentFailedMessage failedMessage =
//            DlqPermanentFailedMessage.of(
//                dlqMessage.getDlqId(),
//                dlqMessage.getOriginalTopic(),
//                dlqMessage.getPayload(),
//                dlqMessage.getExceptionType(),
//                dlqMessage.getExceptionMessage()
//            );
//        log.info("초기 저장한 DlqStatus : {}" , dlqMessage.getDlqStatus());
        return dlqMessage;
    }
}
