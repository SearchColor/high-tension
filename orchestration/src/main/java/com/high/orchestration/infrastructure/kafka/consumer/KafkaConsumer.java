package com.high.orchestration.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.OrderCreateSagaService;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.infrastructure.adaptor.OrderCreateAdapter;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateSuccessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

        private final OrderCreateSagaService orderCreateSagaService;
        private final ObjectMapper objectMapper;
        private final OrderCreateAdapter adapter;

        @KafkaListener(topics = "order-success-create")
        public void orderCreateSuccess(String orderCreateSuccessMessage) {
            log.info("[KafkaConsumer] orderCreateSuccess : orderCreateSuccessMessage: {}", orderCreateSuccessMessage);

            try {
                OrderCreateSuccessMessage message = objectMapper.readValue(orderCreateSuccessMessage, OrderCreateSuccessMessage.class);
                StockDeductionCommandRequest request = adapter.toCommand(message);
                orderCreateSagaService.handlerOrderCreateSuccess(request);
            } catch (Exception e) {
                log.error("[KafkaConsumer] orderCreateSuccess : 메시지 파싱 실패 : {}", orderCreateSuccessMessage, e);
            }

            System.out.println("일단 메시지받기 완료");
        }

        //TODO: 주문 생성 실패 이벤트 구독 로직

}
