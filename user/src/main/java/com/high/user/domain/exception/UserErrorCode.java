package com.high.user.domain.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    // 2xxx: User Service Error Codes
    USER_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),

    // 21xx: Validation Errors
    INVALID_EMAIL_FORMAT(2100, HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다"),
    INVALID_PASSWORD(2101, HttpStatus.BAD_REQUEST, "비밀번호는 필수입니다"),
    DUPLICATE_EMAIL(2102, HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다"),
    INVALID_NAME(2103, HttpStatus.BAD_REQUEST, "이름은 필수입니다"),

    // 22xx: Authentication Errors
    INVALID_CREDENTIALS(2200, HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),

    // 24xx: Account Status Errors
    INACTIVE_ACCOUNT(2400, HttpStatus.FORBIDDEN, "비활성화된 계정입니다"),
    DELETED_ACCOUNT(2401, HttpStatus.FORBIDDEN, "삭제된 계정입니다");

    private final int code;
    private final HttpStatus status;
    private final String message;
}
