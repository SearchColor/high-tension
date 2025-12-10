package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class InvalidRefreshTokenException extends CustomException {

    public InvalidRefreshTokenException() {
        super(UserErrorCode.INVALID_REFRESH_TOKEN);
    }
}