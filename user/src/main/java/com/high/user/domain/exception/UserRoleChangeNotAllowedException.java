package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class UserRoleChangeNotAllowedException extends CustomException {

    public UserRoleChangeNotAllowedException() {
        super(UserErrorCode.ROLE_CHANGE_NOT_ALLOWED);
    }
}