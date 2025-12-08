package com.high.gateway.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Gateway Error Code Enum
 * Gateway에서 발생하는 에러 코드 정의
 * Error Code Range: 9000-9999
 */
@Getter
@RequiredArgsConstructor
public enum GatewayErrorCode implements BaseErrorCode {

    // 90xx: JWT Authentication Errors
    INVALID_TOKEN(9000, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(9001, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다"),
    TOKEN_NOT_FOUND(9002, HttpStatus.UNAUTHORIZED, "요청에 토큰이 없습니다"),
    BLACKLISTED_TOKEN(9003, HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다"),

    // 91xx: Authorization Errors
    UNAUTHORIZED(9100, HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다"),
    FORBIDDEN(9101, HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),

    // 92xx: Rate Limiting Errors
    RATE_LIMIT_EXCEEDED(9200, HttpStatus.TOO_MANY_REQUESTS, "요청 한도를 초과했습니다");

    private final int code;
    private final HttpStatus status;
    private final String message;
}