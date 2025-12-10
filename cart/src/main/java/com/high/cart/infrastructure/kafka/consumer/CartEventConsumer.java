package com.high.cart.infrastructure.kafka.consumer;

import com.high.cart.application.service.CartServiceV1;
import com.high.cart.domain.exception.CartNotFoundException;
import com.high.cart.infrastructure.kafka.dto.ClearCartEventDto;
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
public class CartEventConsumer {

    private final CartServiceV1 cartService;

    @KafkaListener(
            topics = "${kafka.topic.clear-cart}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeClearCartEvent(
            @Payload ClearCartEventDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("=== Kafka Message Received ===");
        log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        log.info("Event: userId={}, orderId={}", event.getUserId(), event.getOrderId());

        try {
            // 장바구니 비우기 처리
            cartService.deleteAllToCart(event.getUserId());

            log.info("✅ Cart cleared successfully for user: {}", event.getUserId());

        } catch (Exception e) {
            log.error("❌ Failed to clear cart for user: {}", event.getUserId(), e);
            throw e;
        }
    }
}
