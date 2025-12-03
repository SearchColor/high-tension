package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class DuplicateEmailException extends CustomException {

    public DuplicateEmailException() {
        super(UserErrorCode.DUPLICATE_EMAIL);
    }
}
