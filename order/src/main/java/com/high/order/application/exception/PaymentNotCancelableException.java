package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class PaymentNotCancelableException extends CustomException {

    public PaymentNotCancelableException() {
        super(OrderErrorCode.PAYMENT_NOT_CANCELABLE);
    }
}
