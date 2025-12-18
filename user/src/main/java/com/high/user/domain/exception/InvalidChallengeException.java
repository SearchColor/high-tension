package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class InvalidChallengeException extends CustomException {
    public InvalidChallengeException() {
        super(UserErrorCode.INVALID_CHALLENGE);
    }
}