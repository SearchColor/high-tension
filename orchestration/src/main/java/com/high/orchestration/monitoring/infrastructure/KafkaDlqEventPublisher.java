package com.high.orchestration.monitoring.infrastructure;

import com.high.orchestration.infrastructure.kafka.producer.KafkaMessageSender;
import com.high.orchestration.monitoring.application.DlqEventPublisher;
import com.high.orchestration.monitoring.application.dto.DlqPermanentFailedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDlqEventPublisher implements DlqEventPublisher {

    private final KafkaMessageSender sender;

    @Override
    public void publishPermanentFailed(String topic, DlqPermanentFailedMessage message) {
        sender.send(topic, message);
        log.info("[DlqEventPublisherAdaptor] publishPermanentFailed 이벤트 발행 성공");

    }
}
