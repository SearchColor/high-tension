package com.high.orchestration.application;

import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderDeleteCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockRestoreCommandRequest;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateSagaService {

    private final SagaStateRepository sagaStateRepository;
    private final EventPublisher publisher;

    @Retryable(
        retryFor = {
            DataAccessException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
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

    @Recover
    private SagaState recoverInitSagaState(DataAccessException e, UUID sagaId, UUID orderId,
        SagaType sagaType, CurrentStep currentStep, String payload) {
        log.error("[OrderCreateSagaService] Saga 초기화 최종 실패 - sagaId: {}, error occurrence time: {}", sagaId,
            LocalDateTime.now(), e);

        throw new FailedToInitializationException();
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
    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 100, multiplier = 1.5)
    )
    public void handlerOrderCreateSuccess(StockDeductionCommandRequest stockRequest) {
        log.info("[OrderCreateSagaService] handlerOrderCreateSuccess - 주문생성완료 후 handler 유입 성공");
        UUID sagaId = stockRequest.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        try {

            if(checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_VALIDATE)) {
                return;
            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_STOCK, stockRequest.toString());
            sagaState.updateOrderId(stockRequest.orderId());


        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("[OrderCreateSagaService] 낙관적 락 충돌 - 재시도: sagaId={}", sagaId);
            throw e; // 재시도를 위해 예외를 다시 던짐


        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerOrderCreateSuccess : 주문 생성 handler처리 실패");
            recordSagaError(sagaState, e);
            throw e;

        }
    }


    @Recover
    public void recoverOrderCreateSuccess(ObjectOptimisticLockingFailureException e,
        StockDeductionCommandRequest stockRequest) {
        log.error("[OrderCreateSagaService] 주문 생성 성공 처리 최종 실패 - sagaId: {}",
            stockRequest.sagaId(), e);

        try {
            SagaState sagaState = getSagaState(stockRequest.sagaId());
            sagaState.fail("동시성 충돌로 인한 상태 업데이트 실패");
            sagaStateRepository.save(sagaState);
        } catch (Exception ex) {
            log.error("[OrderCreateSagaService] Recover 중 오류 발생", ex);
        }
    }

    @Transactional
    @Retryable(
        retryFor = {DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void handleOrderCreateFailed(OrderCreateFailCommandResponse request) {
        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        try {
            sagaState.fail("주문 생성 실패: " + request.reason());
            sagaStateRepository.save(sagaState);
            log.error("[OrderCreateSagaService] handlerOrderCreateFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, request.reason());
        } catch (DataAccessException e) {
            log.warn("[OrderCreateSagaService] DB 저장 실패 - 재시도: sagaId={}", sagaId);
            throw e;

        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerOrderCreateFailed 유입 - 주문 생성 실패 처리 중 오류: sagaId={}", sagaId, e);
        }
    }

    @Transactional
    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 100, multiplier = 1.5)
    )
    public void handlerStockDeductionSuccess(PaymentCreateCommandRequest request) {
        log.info("[OrderCreateSagaService] handlerStockDeductionSuccess - 재고차감 완료 후 handler 유입 성공");
        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        try {
            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_STOCK)) {
                return;
            }

            updateAndSaveSagaState(sagaState, CurrentStep.ORDER_CREATE_PAYMENT, request.toString());

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("[OrderCreateSagaService] 낙관적 락 충돌 - 재시도: sagaId={}", sagaId);
            throw e;


        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerStockDeductionSuccess : 재고 차감 handler처리 실패");
            recordSagaError(sagaState, e);
        }
    }

    @Recover
    public void recoverStockDeductionSuccess(ObjectOptimisticLockingFailureException e,
        PaymentCreateCommandRequest request) {
        log.error("[OrderCreateSagaService] 재고 차감 성공 처리 최종 실패 - sagaId: {}",
            request.sagaId(), e);

        try {
            SagaState sagaState = getSagaState(request.sagaId());
            sagaState.fail("동시성 충돌로 인한 상태 업데이트 실패");
            sagaStateRepository.save(sagaState);
        } catch (Exception ex) {
            log.error("[OrderCreateSagaService] Recover 중 오류 발생", ex);
        }
    }


    @Transactional
    @Retryable(
        retryFor = {DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void handlerStockDeductionFailed(UUID sagaId, String errorMessage) {
        log.info("[OrderCreateSagaService] handlerStockDeductionFailed - 재고차감 실패 후 handler 유입 성공, sagaId : {}", sagaId);
        SagaState sagaState = getSagaState(sagaId);

        try {
            sagaState.fail("재고 차감 실패: " + errorMessage);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] handlerStockDeductionFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, errorMessage);

        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerStockDeductionFailed 유입 - 재고 차감 실패 처리 중 오류: sagaId={}", sagaId, e);
            throw e;

        }
    }


    @Transactional
    @Retryable(
        retryFor = {DataAccessException.class, ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void stockDeductionFailedCompensation(OrderDeleteCommandRequest request) {
        log.info("[OrderCreateSagaService] stockDeductionFailedCompensation - 재고차감 실패 후 보상 트랜잭션 handler 유입 성공");

        UUID sagaId = request.sagaId();
        SagaState sagaState = getSagaState(sagaId);


        try {
            sagaState.updateSagaStatus(SagaStatus.COMPENSATING);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] stockDeductionFailedCompensation - saga 상태 'COMPENSATING' 업데이트 완료");
        } catch (Exception e) {
            log.error("[OrderCreateSagaService] stockDeductionFailedCompensation 유입 - 재고 차감실패 보상트랜잭션 처리 중 오류: sagaId={}", sagaId, e);

        }
    }

    @Recover
    public void recoverStockDeductionFailedCompensation(Exception e, OrderDeleteCommandRequest request) {
        log.error("[OrderCreateSagaService] 보상 트랜잭션 최종 실패 - sagaId: {}",
            request.sagaId(), e);
        //TODO: 보상 트랜잭션 실패는 심각한 문제이므로 슬랙 알림보내기
    }


    @Transactional
    @Retryable(
        retryFor = {DataAccessException.class, ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void PaymentCreateFailedCompensation(OrderDeleteCommandRequest orderRequest,
        StockRestoreCommandRequest stockRequest) {
        log.info("[OrderCreateSagaService] PaymentCreateFailedCompensation - 결제 생성 실패 후 보상 트랜잭션 handler 유입 성공");

        UUID sagaId = orderRequest.sagaId();
        SagaState sagaState = getSagaState(sagaId);

        //TODO:보상 트랜잭션 멱득성 체크 (상태값...추가해야 겠다...-> COMPENSATION_STOCK ..)
        //if (!sagaState..isStockRestored()) ... 확인 메서드도 추가..

        try {
            sagaState.updateSagaStatus(SagaStatus.COMPENSATING);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] PaymentCreateFailedCompensation - saga state 'COMPENSATING' 업데이트 완료");

        } catch (Exception e) {
            log.error("[OrderCreateSagaService] PaymentCreateFailedCompensation 유입 - 결제 생성 실패 보상트랜잭션 처리 중 오류: sagaId={}", sagaId, e);

        }

    }





    //이 메서드는 수정될 예정
    @Transactional
    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 100, multiplier = 1.5)
    )
    public void handlerPaymentCreateSuccess(UUID sagaId, UUID orderId) {
        log.info("[OrderCreateSagaService] handlerPaymentCreateSuccess - 결제생성 완료 후 handler 유입 성공");
        SagaState sagaState = getSagaState(sagaId);

        try {
            if (checkIdempotency(sagaState, CurrentStep.ORDER_CREATE_PAYMENT)) {
                return;
            }
            //TODO: 사가 상태 변경
            updateAndSaveSagaState(sagaState, null, orderId.toString());


        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerPaymentCreateSuccess : 결제 요청 handler처리 실패");
            recordSagaError(sagaState, e);

        }
    }

    @Transactional
    @Retryable(
        retryFor = {DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void handlerPaymentCreateFailed(UUID sagaId, String errorMessage) {

        log.info("[OrderCreateSagaService] handlerPaymentCreateFailed - 결제생성 실패 후 handler 유입 성공");
        SagaState sagaState = getSagaState(sagaId);

        try {
            sagaState.fail("결제 생성 실패: " + errorMessage);
            sagaStateRepository.save(sagaState);
            log.info("[OrderCreateSagaService] handlerPaymentCreateFailed 유입 - 실패상태 업데이트 :  sagaId={}, reason={}",
                sagaId, errorMessage);

        } catch (Exception e) {
            log.error("[OrderCreateSagaService] handlerPaymentCreateFailed 유입 - 결제 생성 실패 처리 중 오류: sagaId={}", sagaId, e);

        }

    }

    @Transactional
    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 100, multiplier = 1.5)
    )
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


    @Recover
    public void recoverEndOrderCreateSaga(ObjectOptimisticLockingFailureException e, UUID sagaId) {
        log.error("[OrderCreateSagaService] Saga 완료 처리 최종 실패 - sagaId: {}", sagaId, e);

        try {
            SagaState sagaState = getSagaState(sagaId);
            sagaState.fail("동시성 충돌로 인한 Saga 완료 처리 실패");
            sagaStateRepository.save(sagaState);
        } catch (Exception ex) {
            log.error("[OrderCreateSagaService] Recover 중 오류 발생", ex);
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

        log.info("[OrderCreateSagaService] saga 상태 업데이트 - sagaId={}, step={}",
            updatedState.getSagaId(), nextStep);

    }

    private void recordSagaError(SagaState sagaState, Exception e) {
        sagaState.recordError(e.getMessage());
        sagaStateRepository.save(sagaState);
    }

}
