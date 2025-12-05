package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class SamePasswordException extends CustomException {

    public SamePasswordException() {
        super(UserErrorCode.SAME_PASSWORD);
    }
}
