package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderItemStatusChangeNotAllowedException extends CustomException {

    public OrderItemStatusChangeNotAllowedException() {
        super(OrderErrorCode.ORDER_ITEM_STATUS_CHANGE_NOT_ALLOWED);
    }
}
