package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class InvalidCredentialsException extends CustomException {

    public InvalidCredentialsException() {
        super(UserErrorCode.INVALID_CREDENTIALS);
    }
}
