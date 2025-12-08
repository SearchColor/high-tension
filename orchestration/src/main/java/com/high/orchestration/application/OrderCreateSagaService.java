package com.high.orchestration.application;

import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponUseCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderDeleteCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.dto.internal.response.OrderCreateFailCommandResponse;
import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.high.orchestration.application.exception.FailedToInitializationException;
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

    private SagaState initSagaState(
        UUID sagaId,
        UUID orderId,
        SagaType sagaType,
        CurrentStep currentStep,
        String payload
    ) {
        try {
            SagaState sagaState = SagaState.create(
                sagaId,
                orderId,
                sagaType,
                currentStep,
                payload,
                null
            );
           return sagaStateRepository.save(sagaState);

        } catch ( Exception e ) {
            log.error("[OrderCreateSagaService] SagaState initialization failed", e);
            throw new FailedToInitializationException();
        }
    }


    @Transactional
    public void startOrderCreateStage(OrderCreateRequest orderCreateRequest, UUID ordererId) {

        //주문 사가 레코드 생성
        UUID sagaId = UUID.randomUUID();

        OrderCreateCommandRequest orderCreateCommandRequest = OrderCreateCommandRequest.from(null, sagaId, ordererId, orderCreateRequest);

        SagaState sagaState = initSagaState(
            sagaId,
            orderCreateCommandRequest.orderId(),
            SagaType.ORDER_CREATE,
            CurrentStep.ORDER_CREATE_VALIDATE,
            orderCreateCommandRequest.toString()
        );

        log.info("[SagaService - startOrderCreateStage] - Saga Started : sagaId={}",
            sagaId);

        try {
            publisher.publishOrderCreateCommand("order-create-request", orderCreateCommandRequest);

        } catch (Exception e) {
            log.info("Saga 시작 실패, 주문 생성 요청 전송 실패");
            recordSagaError(sagaState, e);
            throw new FailedToStartSagaException();
        }
        log.info("주문 생성 saga 시작 성공");
    }

    @Transactional
    public void handlerOrderCreateSuccess(StockDeductionCommandRequest stockRequest, CouponUseCommandRequest commandRequest) {
        log.info("[OrderCreateSagaService] handlerOrderCreateSuccess - 주문생성완료 후 handler 유입 성공");
        UUID sagaId = stockRequest.sagaId();
        SagaState sagaState = getSagaState(sagaId);
        try {

            if(checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_VALIDATE)) {
                return;
            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_STOCK, stockRequest.toString());

            publisher.publishCouponUseCommand("coupon-use-request", commandRequest);
            publisher.publishStockDeductionCommand("stock-deduction-request", stockRequest);
            log.info("[OrderCreateSagaService] handlerOrderCreateSuccess : 재고차감 명령 발행 성공 ");


        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerOrderCreateSuccess : 주문 생성 handler처리 실패");
            recordSagaError(sagaState, e);

        }
    }

    @Transactional
    public void handleOrderCreateFailed(OrderCreateFailCommandResponse request) {
        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        try {
            sagaState.fail("주문 생성 실패: " + request.reason());
            sagaStateRepository.save(sagaState);
            log.error("[OrderCreateSagaService] handlerOrderCreateFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, request.reason());
        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerOrderCreateFailed 유입 - 주문 생성 실패 처리 중 오류: sagaId={}", sagaId, e);
        }
    }

    @Transactional
    public void handlerStockDeductionSuccess(PaymentCreateCommandRequest request) {
        log.info("[OrderCreateSagaService] handlerStockDeductionSuccess - 재고차감 완료 후 handler 유입 성공");
        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        try {
            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_STOCK)) {
                return;
            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_PAYMENT, request.toString());

            publisher.publishPaymentCreateCommand("payment-create-request", request);
            log.info("[OrderCreateSagaService] handlerStockDeductionSuccess : 결제 생성 명령 성공");

        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerStockDeductionSuccess : 재고 차감 handler처리 실패");
            recordSagaError(sagaState, e);
        }
    }

    @Transactional
    public void handlerStockDeductionFailed(OrderDeleteCommandRequest request, String errorMessage) {
        log.info("[OrderCreateSagaService] handlerStockDeductionFailed - 재고차감 실패 후 handler 유입 성공");
        log.info("[OrderCreateSagaService] handlerStockDeductionFailed - sagaId : {}", request.sagaId());
        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        try {
            sagaState.fail("재고 차감 실패: " + errorMessage);
            sagaStateRepository.save(sagaState);
            log.error("[OrderCreateSagaService] handlerStockDeductionFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, errorMessage);

        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerStockDeductionFailed 유입 - 재고 차감 실패 처리 중 오류: sagaId={}", sagaId, e);

        }
    }

    public void stockDeductionFailedCompensation(OrderDeleteCommandRequest request) {
        log.info("[OrderCreateSagaService] stockDeductionFailedCompensation - 재고차감 실패 후 handler 유입 성공");

        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        publisher.publishOrderDeleteCommand("order-delete-request", request);

        try {
            sagaState.updateSagaStatus(SagaStatus.COMPENSATING);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] stockDeductionFailedCompensation - saga 상태 'COMPENSATING' 업데이트 완료");
        } catch (Exception e) {
            log.error("[OrderCreateSagaService] stockDeductionFailedCompensation 유입 - 재고 차감실패 보상트랜잭션 처리 중 오류: sagaId={}", sagaId, e);

        }
    }

    //이 메서드는 수정될 예정
    @Transactional
    public void handlerPaymentCreateSuccess(UUID sagaId, UUID orderId) {
        log.info("[OrderCreateSagaService] handlerPaymentCreateSuccess - 결제생성 완료 후 handler 유입 성공");
        SagaState sagaState = getSagaState(sagaId);

        try {
            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_PAYMENT)) {
                return;
            }

            updateAndSaveSagaState(sagaState, null, orderId.toString());


        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerPaymentCreateSuccess : 결제 요청 handler처리 실패");
            recordSagaError(sagaState, e);

        }
    }

    public void endOrderCreateSaga(UUID sagaId) {
        SagaState sagaState = getSagaState(sagaId);
        try {
            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_COMPLETE)) {
                return;
            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_COMPLETE, sagaId.toString());
            sagaStateRepository.save(sagaState);
        } catch (Exception e) {
            log.error("[OrderCreateSagaService] endOrderCreateSaga : saga 완료 처리 실패");
            recordSagaError(sagaState, e);
        }
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

        log.info("[OrderCreateSagaService] saga 상태 업데이트 - sagaId={}, orderId={}, step={}",
            updatedState.getSagaId(), updatedState.getOrderId(), nextStep);

    }

    private void recordSagaError(SagaState sagaState, Exception e) {
        sagaState.recordError(e.getMessage());
        sagaStateRepository.save(sagaState);
    }


}
