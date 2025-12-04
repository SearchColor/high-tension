package com.high.orchestration.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.high.orchestration.application.exception.SagaStateNotFoundException;
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

    @Transactional
    public void handlerOrderCreateSuccess(StockDeductionCommandRequest message) {
        UUID sagaId = message.sagaId();

        try {
            SagaState sagaState =getSagaState(sagaId);

            if(sagaState.getCurrentStep().isAfter(CurrentStep.ORDER_CREATE_VALIDATE)) {
                log.warn("[Idempotency] 이미 처리된 이벤트: sagaId={}, currentStep={}",
                    sagaId, sagaState.getCurrentStep());
                return;
            }

            sagaState.updateSagaState(
                message.orderId(),
                null,
                CurrentStep.ORDER_CREATE_STOCK,
                message.toString()
            );

            SagaState updatedState = sagaStateRepository.save(sagaState);

            log.info("[OrderCreateSagaService] handlerOrderCreateSuccess : saga 상태 업데이트 - sagaId={}, orderId={}", updatedState.getSagaId(), updatedState.getOrderId());

            publisher.publishStockDeductionCommand("stock-deduction-request", message);
            log.info("[OrderCreateSagaService] handlerOrderCreateSuccess : 재고차감 명령 발행 성공 ");

        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerOrderCreateSuccess : 주문 생성 handler처리 실패");
        }
    }

    public SagaState getSagaState(UUID sagaId) {
        return sagaStateRepository.findById(sagaId).orElseThrow(SagaStateNotFoundException::new);
    }


}
