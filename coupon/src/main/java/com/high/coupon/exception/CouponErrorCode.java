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

    // domain 6500 ~
    COUPON_ISSUE_PERIOD_INVALID(6500, HttpStatus.BAD_REQUEST, "쿠폰 발급 기간이 아닙니다."),
    COUPON_ALREADY_ISSUED(6501, HttpStatus.BAD_REQUEST, "이미 발급이 완료 된 쿠폰입니다."),


    ;

    private final int code;
    private final HttpStatus status;
    private final String message;

}