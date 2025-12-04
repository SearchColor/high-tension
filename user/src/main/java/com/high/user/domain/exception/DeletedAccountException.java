package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class DeletedAccountException extends CustomException {

    public DeletedAccountException() {
        super(UserErrorCode.DELETED_ACCOUNT);
    }
}
