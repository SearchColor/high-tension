package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class InvalidPhoneNumberException extends CustomException {
    public InvalidPhoneNumberException() {
        super(UserErrorCode.INVALID_PHONE_NUMBER);
    }
}
