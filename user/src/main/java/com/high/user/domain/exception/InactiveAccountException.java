package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class InactiveAccountException extends CustomException {

    public InactiveAccountException() {
        super(UserErrorCode.INACTIVE_ACCOUNT);
    }
}
