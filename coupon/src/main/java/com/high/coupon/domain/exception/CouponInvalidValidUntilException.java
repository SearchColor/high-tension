package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponInvalidValidUntilException extends CustomException {

    public CouponInvalidValidUntilException() {
        super(CouponErrorCode.COUPON_INVALID_VALID_UNTIL);
    }
}
