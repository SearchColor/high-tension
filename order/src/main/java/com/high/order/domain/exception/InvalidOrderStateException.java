package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class InvalidOrderStateException extends CustomException {

    public InvalidOrderStateException() {
        super(OrderErrorCode.INVALID_ORDER_STATUS);
    }
}
