package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponNotOwnedException extends CustomException {

    public CouponNotOwnedException() {
        super(CouponErrorCode.COUPON_NOT_OWNED);
    }
}
