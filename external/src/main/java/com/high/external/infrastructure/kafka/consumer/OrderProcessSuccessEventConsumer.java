package com.high.external.infrastructure.kafka.consumer;

import com.high.external.application.service.SlackMessageServiceV1;
import com.high.external.infrastructure.kafka.dto.OrderProcessSuccessEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProcessSuccessEventConsumer {

    private final SlackMessageServiceV1 messageServiceV1;

    @KafkaListener(
            topics = "${kafka.topic.order-process-success}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumerOrderProcessSuccessEvent(
            @Payload OrderProcessSuccessEventDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("=== Kafka Message Received ===");
        log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        log.info("Event: userId={}, orderId={}", event.getUserId(), event.getOrderId());

        try {
            // 주문 상품명 포맷팅 (예: 샤프 외 1건)
            String itemSummary = "";
            List<String> items = event.getOrderItemNameList();

            if (items != null && !items.isEmpty()) {
                String firstItem = items.get(0);
                int otherCount = items.size() - 1;
                itemSummary = (otherCount > 0)
                        ? String.format("%s 외 %d건", firstItem, otherCount)
                        : firstItem;
            }

            // 메시지 본문 생성
            String message = String.format(
                    "주문이 완료되었습니다.\n\n" +
                            "주문 날짜: %s\n" +
                            "주문 번호: %s\n" +
                            "주문 상품: %s\n" +
                            "주문 총액: %,d원\n\n" +
                            "배송이 시작되면 다시 안내드리겠습니다.\n" +
                            "감사합니다.",
                    event.getCreatedAt(),
                    event.getOrderId(),
                    itemSummary,
                    event.getPaidAmount()
            );

            // 슬랙 전송 (채널 ID 사용)
            messageServiceV1.slackMessageSend("C0A3XR53W2K", message);

            log.info("✅ Slack message sent for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ Failed to process order event: {}", event.getOrderId(), e);
            throw e;
        }
    }
}
