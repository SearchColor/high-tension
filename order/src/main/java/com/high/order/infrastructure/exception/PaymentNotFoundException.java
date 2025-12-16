package com.high.order.infrastructure.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class PaymentNotFoundException extends CustomException {

    public PaymentNotFoundException() {
        super(OrderErrorCode.PAYMENT_NOT_FOUND);
    }
}
