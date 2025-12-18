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
    INVALID_PHONE_NUMBER(2104, HttpStatus.BAD_REQUEST, "전화번호 형식이 올바르지 않습니다"),
    SAME_PASSWORD(2105, HttpStatus.BAD_REQUEST, "현재 비밀번호와 동일한 비밀번호입니다"),

    // 22xx: Authentication Errors
    INVALID_CREDENTIALS(2200, HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
    INVALID_TOKEN(2201, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    INVALID_REFRESH_TOKEN(2202, HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다"),

    // 23xx: Admin Errors
    ROLE_CHANGE_NOT_ALLOWED(2300, HttpStatus.FORBIDDEN, "권한을 변경할 수 없습니다"),

    // 24xx: Account Status Errors
    INACTIVE_ACCOUNT(2400, HttpStatus.FORBIDDEN, "비활성화된 계정입니다"),
    DELETED_ACCOUNT(2401, HttpStatus.FORBIDDEN, "삭제된 계정입니다"),
    ALREADY_DELETED(2402, HttpStatus.GONE, "이미 탈퇴한 계정입니다"),

    // 25xx: External Service Errors
    COUPON_SERVICE_ERROR(2500, HttpStatus.SERVICE_UNAVAILABLE, "쿠폰 서비스에 문제가 발생했습니다"),

    // 26xx: Passkey Errors
    PASSKEY_NOT_FOUND(2600, HttpStatus.NOT_FOUND, "패스키를 찾을 수 없습니다"),
    INVALID_CHALLENGE(2601, HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 Challenge입니다"),
    WEBAUTHN_VERIFICATION_FAILED(2602, HttpStatus.UNAUTHORIZED, "패스키 인증에 실패했습니다"),
    PASSKEY_ALREADY_EXISTS(2603, HttpStatus.CONFLICT, "이미 등록된 패스키입니다"),
    PASSKEY_REGISTRATION_FAILED(2604, HttpStatus.BAD_REQUEST, "패스키 등록에 실패했습니다"),
    NO_PASSKEY_REGISTERED(2605, HttpStatus.NOT_FOUND, "등록된 패스키가 없습니다"),
    PASSKEY_SERIALIZATION_FAILED(2606, HttpStatus.INTERNAL_SERVER_ERROR, "패스키 데이터 직렬화에 실패했습니다"),
    INVALID_CREDENTIAL_FORMAT(2607, HttpStatus.INTERNAL_SERVER_ERROR, "패스키 데이터 형식이 올바르지 않습니다"),
    INVALID_CHALLENGE_DATA(2608, HttpStatus.BAD_REQUEST, "Challenge 데이터가 유효하지 않습니다"),
    INVALID_REQUEST_DATA(2609, HttpStatus.BAD_REQUEST, "요청 데이터가 유효하지 않습니다");

    private final int code;
    private final HttpStatus status;
    private final String message;
}
