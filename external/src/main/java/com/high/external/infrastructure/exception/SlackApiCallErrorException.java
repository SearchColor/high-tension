package com.high.external.infrastructure.exception;

import com.high.external.exception.ExternalErrorCode;
import com.library.module.exception.CustomException;

public class SlackApiCallErrorException extends CustomException {
    public SlackApiCallErrorException() {
        super(ExternalErrorCode.SLACK_API_CALL_ERROR);
    }

}
