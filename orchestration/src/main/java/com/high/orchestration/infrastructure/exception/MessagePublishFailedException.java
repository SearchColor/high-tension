package com.high.orchestration.infrastructure.exception;

import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.CustomException;

public class MessagePublishFailedException extends CustomException {

    public MessagePublishFailedException(Throwable cause) {
        super(OrchestrationErrorCode.MESSAGE_PUBLISH_FAILED_EXCEPTION);
        initCause(cause);
    }
}
