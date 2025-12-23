package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderItemNotFoundInOrderException extends CustomException {

    public OrderItemNotFoundInOrderException() {
        super(OrderErrorCode.ORDER_ITEM_NOT_FOUND_IN_ORDER);
    }
}
