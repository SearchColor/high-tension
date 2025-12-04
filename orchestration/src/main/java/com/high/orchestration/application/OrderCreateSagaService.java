package com.high.orchestration.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.high.orchestration.application.port.EventPublisher;
import com.high.orchestration.domain.entity.SagaState;
import com.high.orchestration.domain.repository.SagaStateRepository;
import com.high.orchestration.domain.vo.CurrentStep;
import com.high.orchestration.domain.vo.SagaType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateSagaService {

    private final SagaStateRepository sagaStateRepository;
    private final ObjectMapper objectMapper;
    private final EventPublisher publisher;

    @Transactional
    public void startOrderCreateStage(OrderCreateRequest orderCreateRequest, UUID ordererId) {

        //주문 사가 레코드 생성
        UUID sagaId = UUID.randomUUID();

        OrderCreateCommandRequest orderCreateCommandRequest = OrderCreateCommandRequest.from(null, sagaId, ordererId, orderCreateRequest);
        try {
            SagaState sagaState = SagaState.create(
                orderCreateCommandRequest.sagaId(),
                orderCreateCommandRequest.orderId(),
                SagaType.ORDER_CREATE,
                CurrentStep.ORDER_CREATE_VALIDATE,
                objectMapper.writeValueAsString(orderCreateCommandRequest),
                null
            );

            sagaStateRepository.save(sagaState);
            System.out.println("받은 데이터 : " + objectMapper.writeValueAsString(
                orderCreateCommandRequest));
            log.info("[SagaService - startOrderCreateStage] - Saga Started : sagaId={}",
                sagaId);

            publisher.publishOrderCreateCommand("order-create-request", orderCreateCommandRequest);


        } catch (Exception e) {
            log.info("Saga 시작 실패, 주문 생성 요청 전송 실패");
            //exception 만들기
            //kafka 발핼
        }
        log.info("주문 생성 saga 시작 성공");
    }
}
