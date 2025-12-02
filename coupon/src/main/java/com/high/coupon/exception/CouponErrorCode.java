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

    // application
    COUPON_NOT_FOUND(4000, HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),


    // domain



    ;

    private final int code;
    private final HttpStatus status;
    private final String message;

}