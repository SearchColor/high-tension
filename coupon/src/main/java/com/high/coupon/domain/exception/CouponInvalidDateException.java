package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponInvalidDateException extends CustomException {

    public CouponInvalidDateException() {
        super(CouponErrorCode.COUPON_INVALID_DATE);
    }
}
