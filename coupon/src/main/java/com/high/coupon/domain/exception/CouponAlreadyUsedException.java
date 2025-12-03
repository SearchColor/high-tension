package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponAlreadyUsedException extends CustomException {

    public CouponAlreadyUsedException() {
        super(CouponErrorCode.COUPON_ALREADY_USED);
    }
}
