package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponInvalidDiscountRateException extends CustomException {

    public CouponInvalidDiscountRateException() {
        super(CouponErrorCode.COUPON_INVALID_DISCOUNT_RATE);
    }
}
