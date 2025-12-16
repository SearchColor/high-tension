package com.high.orchestration.infrastructure.exception;

import com.high.orchestration.exception.OrchestrationErrorCode;
import com.library.module.exception.CustomException;

public class MessageSerializationException extends CustomException {

    public MessageSerializationException(Throwable cause) {
        super(OrchestrationErrorCode.MESSAGE_SERIALIZATION_EXCEPTION);
        initCause(cause);
    }
}
