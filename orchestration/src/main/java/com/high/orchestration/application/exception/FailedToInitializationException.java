package com.high.orchestration.application.exception;

import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.CustomException;

public class FailedToInitializationException extends CustomException {

    public FailedToInitializationException() {
        super(OrchestrationErrorCode.FAILED_TO_INITIALIZATION);
    }
}
