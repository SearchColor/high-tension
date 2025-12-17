package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class PasskeyAlreadyExistsException extends CustomException {
    public PasskeyAlreadyExistsException() {
        super(UserErrorCode.PASSKEY_ALREADY_EXISTS);
    }
}