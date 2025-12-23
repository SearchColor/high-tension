package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderItemNotFoundException extends CustomException {

    public OrderItemNotFoundException() {
        super(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
    }
}
