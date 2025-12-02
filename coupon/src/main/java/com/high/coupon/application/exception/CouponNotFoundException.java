package com.high.coupon.application.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponNotFoundException extends CustomException {
    public CouponNotFoundException(){
        super(CouponErrorCode.COUPON_NOT_FOUND);
    }
}