package com.high.coupon.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements BaseErrorCode {

    /**
     * COUPON - error 6000번대
     */

    // application 6000 ~
    COUPON_NOT_FOUND(6000, HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰입니다."),
    COUPON_OUT_OF_STOCK(6001, HttpStatus.BAD_REQUEST, "쿠폰이 모두 소진되었습니다."),
    COUPON_ISSUE_NOT_FOUND(6002, HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰 발급 내역입니다."),

    // domain 6500 ~ todo (user 연동 후 확인 필요)
    COUPON_ISSUE_PERIOD_INVALID(6500, HttpStatus.BAD_REQUEST, "쿠폰 발급 기간이 아닙니다."),
    COUPON_ALREADY_ISSUED(6501, HttpStatus.BAD_REQUEST, "이미 발급이 완료 된 쿠폰입니다."),
    COUPON_ALREADY_USED(6502, HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    COUPON_NOT_OWNED(6503, HttpStatus.FORBIDDEN, "쿠폰 사용 권한이 없습니다."),

    COUPON_NOT_VALID_PERIOD(6504, HttpStatus.BAD_REQUEST, "쿠폰 사용 가능 기간이 아닙니다. 유효 기간을 확인해주세요."),
    COUPON_INVALID_DATE(6505, HttpStatus.BAD_REQUEST, "쿠폰 발행/유효 기간을 입력해주세요"),
    COUPON_INVALID_DISCOUNT_RATE(6506, HttpStatus.BAD_REQUEST, "쿠폰 할인율은 1~100% 사이여야 하며, 소수점은 허용되지 않습니다."),

    COUPON_EXPIRED(6505, HttpStatus.BAD_REQUEST, "쿠폰 사용 기간이 만료되어 복원되지 않습니다."),

    COUPON_INVALID_ISSUE_PERIOD(6507, HttpStatus.BAD_REQUEST, "발행 종료일이 시작일보다 이전입니다."),
    COUPON_INVALID_VALID_UNTIL(6508, HttpStatus.BAD_REQUEST, "유효기간이 발행 종료일보다 이전입니다."),

    ;
    private final int code;
    private final HttpStatus status;
    private final String message;

}