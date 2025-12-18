package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class InvalidCredentialFormatException extends CustomException {
    public InvalidCredentialFormatException() {
        super(UserErrorCode.INVALID_CREDENTIAL_FORMAT);
    }
}