package com.high.orchestration.application;

import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderDeleteCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockRestoreCommandRequest;
import com.high.orchestration.application.dto.internal.response.OrderCreateFailCommandResponse;
import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.high.orchestration.application.exception.FailedToStartSagaException;
import com.high.orchestration.application.exception.SagaStateNotFoundException;
import com.high.orchestration.application.port.EventPublisher;
import com.high.orchestration.domain.entity.SagaState;
import com.high.orchestration.domain.repository.SagaStateRepository;
import com.high.orchestration.domain.vo.CurrentStep;
import com.high.orchestration.domain.vo.SagaStatus;
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
    private final EventPublisher publisher;

    public SagaState initSagaState(
        UUID sagaId,
        UUID orderId,
        SagaType sagaType,
        CurrentStep currentStep,
        String payload
    ) {
        SagaState sagaState = SagaState.create(
            sagaId,
            orderId,
            sagaType,
            currentStep,
            payload,
            null
        );
       return sagaStateRepository.save(sagaState);
    }



    @Transactional
    public void startOrderCreateStage(OrderCreateRequest orderCreateRequest, UUID userId) {

        //주문 사가 레코드 생성
        UUID sagaId = UUID.randomUUID();

        OrderCreateCommandRequest orderCreateCommandRequest = OrderCreateCommandRequest.from(null, sagaId, userId, orderCreateRequest);

        SagaState sagaState = initSagaState(
            sagaId,
            orderCreateCommandRequest.orderId(),
            SagaType.ORDER_CREATE,
            CurrentStep.ORDER_CREATE_VALIDATE,
            orderCreateCommandRequest.toString()
        );

        log.info("[SagaService - startOrderCreateStage] - Saga Started : sagaId={}, userId= {}",
            sagaId, orderCreateCommandRequest.ordererId());

        try {
            publisher.publishOrderCreateCommand("order-create-request", orderCreateCommandRequest);

        } catch (Exception e) {
            log.error("Saga 시작 실패, 주문 생성 요청 전송 실패");
            recordSagaError(sagaState, e);
            throw new FailedToStartSagaException();
        }
        log.info("주문 생성 saga 시작 성공");
    }

    @Transactional
    public void handlerOrderCreateSuccess(StockDeductionCommandRequest stockRequest) {
        log.info("[OrderCreateSagaService] handlerOrderCreateSuccess - 주문생성완료 후 handler 유입 성공");
        UUID sagaId = stockRequest.sagaId();
        SagaState sagaState = getSagaState(sagaId);

            if(checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_VALIDATE)) {
                log.info("[OrderCreateSagaService] handlerOrderCreateSuccess 이미 처리됨 - sagaId={}", sagaId);
                return;
            }
//            if(sagaId != null) {
//                throw new RuntimeException("테스트용 강제 예외 발생!");
//            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_STOCK, stockRequest.toString());
            sagaState.updateOrderId(stockRequest.orderId());
            sagaStateRepository.save(sagaState);


    }


    @Transactional
    public void handleOrderCreateFailed(OrderCreateFailCommandResponse request) {
        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

            sagaState.fail("주문 생성 실패: " + request.reason());
            sagaStateRepository.save(sagaState);
            log.error("[OrderCreateSagaService] handlerOrderCreateFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, request.reason());

    }

    @Transactional
    public void handlerStockDeductionSuccess(PaymentCreateCommandRequest request) {
        log.info("[OrderCreateSagaService] handlerStockDeductionSuccess - 재고차감 완료 후 handler 유입 성공");
        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_STOCK)) {
                log.info("[OrderCreateSagaService] handlerStockDeductionSuccess 이미 처리됨 - sagaId={}", sagaId);
                return;
            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_PAYMENT, request.toString());

    }


    @Transactional
    public void handlerStockDeductionFailed(UUID sagaId, String errorMessage) {
        log.info("[OrderCreateSagaService] handlerStockDeductionFailed - 재고차감 실패 후 handler 유입 성공, sagaId : {}", sagaId);
        SagaState sagaState = getSagaState(sagaId);

            sagaState.fail("재고 차감 실패: " + errorMessage);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] handlerStockDeductionFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, errorMessage);

    }


    @Transactional
    public void stockDeductionFailedCompensation(OrderDeleteCommandRequest request) {
        log.info("[OrderCreateSagaService] stockDeductionFailedCompensation - 재고차감 실패 후 보상 트랜잭션 handler 유입 성공");

        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);


            if (sagaState.getSagaStatus() == SagaStatus.COMPENSATING
                || sagaState.getSagaStatus() == SagaStatus.COMPENSATED) {
                log.warn("[OrderCreateSagaService] stockDeductionFailedCompensation 이미 보상 처리 중/완료 - sagaId={}", sagaId);
                return;
            }
            sagaState.updateSagaStatus(SagaStatus.COMPENSATING);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] stockDeductionFailedCompensation - saga 상태 'COMPENSATING' 업데이트 완료");

    }


    @Transactional
    public void PaymentCreateFailedCompensation(OrderDeleteCommandRequest orderRequest,
        StockRestoreCommandRequest stockRequest) {
        log.info(
            "[OrderCreateSagaService] PaymentCreateFailedCompensation - 결제 생성 실패 후 보상 트랜잭션 handler 유입 성공");

        UUID sagaId = orderRequest.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        //TODO:보상 트랜잭션 멱득성 체크 (상태값...추가해야 겠다...-> COMPENSATION_STOCK ..)
        //if (!sagaState..isStockRestored()) ... 확인 메서드도 추가..

        if (sagaState.getSagaStatus() == SagaStatus.COMPENSATING
            || sagaState.getSagaStatus() == SagaStatus.COMPENSATED) {
            log.warn("[paymentCreateFailedCompensation] 이미 보상 처리 중/완료 - sagaId={}", sagaId);
            return;
        }
        sagaState.updateSagaStatus(SagaStatus.COMPENSATING);
        sagaStateRepository.save(sagaState);
        log.info(
            "[OrderCreateSagaService] PaymentCreateFailedCompensation - saga state 'COMPENSATING' 업데이트 완료");
    }


    //이 메서드는 수정될 예정
    @Transactional
    public void handlerPaymentCreateSuccess(UUID sagaId, UUID orderId) {
        log.info("[OrderCreateSagaService] handlerPaymentCreateSuccess - 결제생성 완료 후 handler 유입 성공");
        SagaState sagaState = getSagaState(sagaId);

            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_PAYMENT)) {
                log.info("[handlerPaymentCreateSuccess] 이미 처리됨 - sagaId={}", sagaId);
                return;
            }
            updateAndSaveSagaState(sagaState, null, orderId.toString());
    }


    @Transactional
    public void handlerPaymentCreateFailed(UUID sagaId, String errorMessage) {

        log.info("[OrderCreateSagaService] handlerPaymentCreateFailed - 결제생성 실패 후 handler 유입 성공");

        SagaState sagaState = getSagaState(sagaId);

            sagaState.fail("결제 생성 실패: " + errorMessage);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] handlerPaymentCreateFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, errorMessage);


    }

    @Transactional
    public void endOrderCreateSaga(UUID sagaId) {
        log.info("[OrderCreateSagaService] endOrderCreateSaga 시작 - sagaId={}", sagaId);

        SagaState sagaState = getSagaState(sagaId);

            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_COMPLETE)) {
                log.info("[OrderCreateSagaService] endOrderCreateSaga - 이미 완료됨 - sagaId={}", sagaId);
                return;
            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_COMPLETE, sagaId.toString());
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] endOrderCreateSaga  완료 - sagaId={}", sagaId);

    }



    public SagaState getSagaState(UUID sagaId) {
        return sagaStateRepository.findById(sagaId).orElseThrow(SagaStateNotFoundException::new);
    }

    private boolean checkIdempotency(SagaState sagaState, CurrentStep expectedStep) {
        if (sagaState.getCurrentStep().isAfter(expectedStep)) {
            log.warn("[Idempotency] 이미 처리된 이벤트: sagaId={}, currentStep={}",
                sagaState.getSagaId(), sagaState.getCurrentStep());
            return true;
        }
        return false;
    }

    private void updateAndSaveSagaState(SagaState sagaState, CurrentStep nextStep, String payload) {
        sagaState.updateSagaState(null, nextStep, payload);
        SagaState updatedState = sagaStateRepository.save(sagaState);

        log.info("[OrderCreateSagaService] saga 상태 업데이트 - sagaId={}, step={}",
            updatedState.getSagaId(), nextStep);

    }

    private void recordSagaError(SagaState sagaState, Exception e) {
        sagaState.recordError(e.getMessage());
        sagaStateRepository.save(sagaState);
    }

}
