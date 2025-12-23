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
    RATE_LIMIT_EXCEEDED(9200, HttpStatus.TOO_MANY_REQUESTS, "요청 한도를 초과했습니다"),

    // 93xx: Circuit Breaker Errors
    SERVICE_UNAVAILABLE(9300, HttpStatus.SERVICE_UNAVAILABLE, "서비스가 일시적으로 사용할 수 없습니다"),
    CIRCUIT_BREAKER_OPEN(9301, HttpStatus.SERVICE_UNAVAILABLE, "서비스 보호를 위해 요청이 차단되었습니다"),
    SERVICE_TIMEOUT(9302, HttpStatus.GATEWAY_TIMEOUT, "서비스 응답 시간을 초과했습니다"),

    // 95xx: Server Errors
    INTERNAL_SERVER_ERROR(9500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),
    CONNECTION_TIMEOUT(9503, HttpStatus.SERVICE_UNAVAILABLE, "서비스에 연결할 수 없습니다"),
    GATEWAY_TIMEOUT(9504, HttpStatus.GATEWAY_TIMEOUT, "요청 시간이 초과되었습니다");

    private final int code;
    private final HttpStatus status;
    private final String message;
}