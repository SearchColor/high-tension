package com.high.gateway.exception;

import com.library.module.exception.CustomException;

/**
 * Unauthorized Exception (401)
 * 인증 실패 시 발생하는 예외
 */
public class UnauthorizedException extends CustomException {

    public UnauthorizedException(GatewayErrorCode errorCode) {
        super(errorCode);
    }
}
