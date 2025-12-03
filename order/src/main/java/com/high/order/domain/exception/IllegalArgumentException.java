package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class IllegalArgumentException extends CustomException {

    public IllegalArgumentException() {
        super(OrderErrorCode.ILLEGAL_ARGUMENT_EXCEPTION);
    }
}
