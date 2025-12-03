package com.high.coupon.application.exception;

import com.high.coupon.exception.CouponErrorCode;
import com.library.module.exception.CustomException;

public class CouponIssueNotFoundException extends CustomException {

    public CouponIssueNotFoundException() {
        super(CouponErrorCode.COUPON_ISSUE_NOT_FOUND);
    }
}
