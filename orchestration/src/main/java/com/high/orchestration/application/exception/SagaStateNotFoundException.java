package com.high.orchestration.application.exception;

import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.CustomException;

public class SagaStateNotFoundException extends CustomException {

    public SagaStateNotFoundException() {
        super(OrchestrationErrorCode.SAGA_STATE_NOT_FOUND);
    }
}
