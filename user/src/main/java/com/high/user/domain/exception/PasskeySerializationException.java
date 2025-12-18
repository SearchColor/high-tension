package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class PasskeySerializationException extends CustomException {
    public PasskeySerializationException() {
        super(UserErrorCode.PASSKEY_SERIALIZATION_FAILED);
    }
}