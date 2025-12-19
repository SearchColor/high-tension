package com.high.orchestration.infrastructure.exception;

import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.CustomException;

public class MessagePublishInterruptedException extends CustomException
{

    public MessagePublishInterruptedException(Throwable cause) {
        super(OrchestrationErrorCode.MESSAGE_PUBLISH_INTERRUPTED_EXCEPTION);
        initCause(cause);
    }
}
