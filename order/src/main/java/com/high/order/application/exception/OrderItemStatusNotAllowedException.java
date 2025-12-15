package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderItemStatusNotAllowedException extends CustomException {

    public OrderItemStatusNotAllowedException() {
        super(OrderErrorCode.ORDER_ITEM_STATUS_CHANGE_NOT_ALLOWED);
    }
}
