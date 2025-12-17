package com.high.order.infrastructure.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class CouponNotFoundException extends CustomException {

    public CouponNotFoundException() {
        super(OrderErrorCode.COUPON_NOT_FOUND);
    }
}
