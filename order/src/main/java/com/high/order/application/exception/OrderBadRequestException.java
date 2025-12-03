package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderBadRequestException extends CustomException {

    public OrderBadRequestException() {
        super(OrderErrorCode.BAD_REQUEST);
    }
}
