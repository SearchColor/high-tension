package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}
