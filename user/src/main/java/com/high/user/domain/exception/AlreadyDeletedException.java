package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class AlreadyDeletedException extends CustomException {

    public AlreadyDeletedException() {
        super(UserErrorCode.ALREADY_DELETED);
    }
}
