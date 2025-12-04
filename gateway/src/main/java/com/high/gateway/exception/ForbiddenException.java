package com.high.gateway.exception;

import com.library.module.exception.CustomException;

/**
 * Forbidden Exception (403)
 * 권한 부족 시 발생하는 예외
 */
public class ForbiddenException extends CustomException {

    public ForbiddenException(GatewayErrorCode errorCode) {
        super(errorCode);
    }
}