package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class CouponServiceException extends CustomException {

    public CouponServiceException() {
        super(UserErrorCode.COUPON_SERVICE_ERROR);
    }
}