package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderCancellationException extends CustomException {

    public OrderCancellationException() {
        super(OrderErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
    }
}
