package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponExpiredException extends CustomException {

    public CouponExpiredException() {
        super(CouponErrorCode.COUPON_EXPIRED);
    }
}
