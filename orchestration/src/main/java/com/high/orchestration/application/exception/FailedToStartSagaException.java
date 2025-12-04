package com.high.orchestration.application.exception;

import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.CustomException;

public class FailedToStartSagaException extends CustomException {

    public FailedToStartSagaException() {
        super(OrchestrationErrorCode.FAILED_TO_START_SAGA);
    }
}
