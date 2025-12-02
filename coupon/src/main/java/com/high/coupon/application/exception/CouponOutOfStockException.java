package com.high.coupon.application.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponOutOfStockException extends CustomException {
    public CouponOutOfStockException() {
        super(CouponErrorCode.COUPON_OUT_OF_STOCK);
    }
}
