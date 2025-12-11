package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

/**
 * Entity 레벨에서 비밀번호 검증 실패 시 발생하는 예외
 */
public class InvalidPasswordException extends CustomException {

    public InvalidPasswordException() {
        super(UserErrorCode.INVALID_PASSWORD);
    }
}
