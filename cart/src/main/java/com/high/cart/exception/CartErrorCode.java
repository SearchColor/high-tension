package com.high.cart.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements BaseErrorCode {

    /**
     * 공통 error -> 1000번대
     */
    INTERNAL_ERROR(7000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    INVALID_INPUT(7001, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(7002, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(7003, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(7004, HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),
    CONFLICT(7005,HttpStatus.CONFLICT,"이미 존재하는 리소스 입니다.")
    // 추가 가능 (전역 예외만)


    ;

    private final int code;
    private final HttpStatus status;
    private final String message;
}
