package com.high.orchestration.infrastructure.exception;

import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.BaseErrorCode;
import com.library.module.exception.CustomException;

public class FailToConvertMessageException extends CustomException {

    public FailToConvertMessageException() {
        super(OrchestrationErrorCode.FAILED_TO_CONVERT_MESSAGE);
    }
}
