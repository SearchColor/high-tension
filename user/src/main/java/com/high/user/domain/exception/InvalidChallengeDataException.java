package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class InvalidChallengeDataException extends CustomException {
    public InvalidChallengeDataException() {
        super(UserErrorCode.INVALID_CHALLENGE_DATA);
    }
}