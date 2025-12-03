package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponNotValidPeriodException extends CustomException {

    public CouponNotValidPeriodException() {
        super(CouponErrorCode.COUPON_NOT_VALID_PERIOD);
    }
}
