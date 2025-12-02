package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponAlreadyIssuedException extends CustomException {
    public CouponAlreadyIssuedException() {
        super(CouponErrorCode.COUPON_ALREADY_ISSUED);
    }
}
