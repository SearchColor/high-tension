package com.high.external.domain.exception;

import com.high.external.exception.ExternalErrorCode;
import com.library.module.exception.CustomException;

public class SlackRecordNotFoundException extends CustomException {
    public SlackRecordNotFoundException() {
        super(ExternalErrorCode.SlACK_RECORD_NOT_FOUND);
    }
}