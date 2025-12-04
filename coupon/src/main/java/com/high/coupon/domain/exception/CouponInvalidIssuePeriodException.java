package com.high.coupon.domain.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponInvalidIssuePeriodException extends CustomException {

    public CouponInvalidIssuePeriodException() {
        super(CouponErrorCode.COUPON_INVALID_ISSUE_PERIOD);
    }
}
