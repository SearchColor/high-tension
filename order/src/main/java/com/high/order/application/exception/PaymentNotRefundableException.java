package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class PaymentNotRefundableException extends CustomException {

    public PaymentNotRefundableException() {
        super(OrderErrorCode.PAYMENT_NOT_REFUNDABLE);
    }
}
