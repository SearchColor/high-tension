package com.high.external.infrastructure.exception;

import com.high.external.exception.ExternalErrorCode;
import com.library.module.exception.CustomException;

public class SlackApiNotificationFailException extends CustomException {
    public SlackApiNotificationFailException(){
        super(ExternalErrorCode.SLACK_NOTIFICATION_FAILED);
    }
}
