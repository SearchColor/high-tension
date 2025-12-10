package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

/**
 * Entity 레벨에서 이름 검증 실패 시 발생하는 예외
 */
public class InvalidNameException extends CustomException {

    public InvalidNameException() {
        super(UserErrorCode.INVALID_NAME);
    }
}
