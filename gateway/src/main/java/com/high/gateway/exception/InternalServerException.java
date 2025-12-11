package com.high.gateway.exception;

import com.library.module.exception.CustomException;

/**
 * Internal Server Exception
 *
 * 예상하지 못한 서버 내부 오류가 발생했을 때 사용하는 예외입니다.
 * 500 Internal Server Error 응답을 제공합니다.
 */
public class InternalServerException extends CustomException {

    /**
     * 기본 생성자
     */
    public InternalServerException() {
        super(GatewayErrorCode.INTERNAL_SERVER_ERROR);
    }
}