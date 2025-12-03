package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponIssuePeriodInvalidException extends CustomException {
    public CouponIssuePeriodInvalidException() {
        super(CouponErrorCode.COUPON_ISSUE_PERIOD_INVALID);
    }
}
