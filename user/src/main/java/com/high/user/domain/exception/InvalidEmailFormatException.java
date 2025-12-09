package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

/**
 * Entity 레벨에서 이메일 형식 검증 실패 시 발생하는 예외
 */
public class InvalidEmailFormatException extends CustomException {

    public InvalidEmailFormatException() {
        super(UserErrorCode.INVALID_EMAIL_FORMAT);
    }
}
