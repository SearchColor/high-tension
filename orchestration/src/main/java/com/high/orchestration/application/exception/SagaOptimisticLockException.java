package com.high.orchestration.application.exception;

import com.high.orchestration.domain.vo.CurrentStep;
import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.CustomException;
import java.util.UUID;



public class SagaOptimisticLockException extends CustomException {
    private final CurrentStep currentStep;
    private final UUID sagaId;


    public SagaOptimisticLockException(
        CurrentStep currentStep,
        UUID sagaId,
        Throwable cause
    ) {
        super(OrchestrationErrorCode.SAGA_OPTIMISTIC_LOCK_EXCEPTION);
        this.currentStep = currentStep;
        this.sagaId = sagaId;
    }


    public CurrentStep getCurrentStep() {
        return currentStep;
    }

    public UUID getSagaId() {
        return sagaId;
    }
}
