package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class PasskeyNotFoundException extends CustomException {
    public PasskeyNotFoundException() {
        super(UserErrorCode.PASSKEY_NOT_FOUND);
    }
}