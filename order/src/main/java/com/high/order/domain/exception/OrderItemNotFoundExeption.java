package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderItemNotFoundExeption extends CustomException {

    public OrderItemNotFoundExeption() {
        super(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
    }
}
