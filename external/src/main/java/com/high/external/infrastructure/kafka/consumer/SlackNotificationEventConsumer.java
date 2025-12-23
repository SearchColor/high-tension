package com.high.external.infrastructure.kafka.consumer;

import com.high.external.application.service.SlackMessageServiceV1;
import com.high.external.infrastructure.kafka.dto.SlackNotificationEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackNotificationEventConsumer {

    private final SlackMessageServiceV1 messageServiceV1;

    @KafkaListener(
            topics = "alert-slack-topic",
            groupId = "slack-service-group-v2",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumerSlackNotificationEvent(
            @Payload SlackNotificationEventDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("=== Kafka Message Received ===");
        log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);

        try {

            // 메시지 본문 생성
            String message = String.format(
                    "에러가 발생되었습니다.\n\n" +
                            "service-name: %s\n" +
                            "api-url: %s\n" +
                            "message: %s\n" +
                            "timestamp: %s\n\n" +
                            "해당 서비스 담당자분께서는 확인 바랍니다.",
                    event.getServiceName(),
                    event.getUrlPath(),
                    event.getMessage(),
                    event.getTimestamp()
            );

            // 슬랙 전송 (채널 ID 사용)
            messageServiceV1.slackMessageSend(event.getTargetSlackId(), message);

            log.info("✅ Slack message sent for channel: {}", event.getTargetSlackId());

        } catch (Exception e) {
            log.error("❌ Failed to message sent for channel: {}", event.getTargetSlackId(), e);
            throw e;
        }
    }
}
