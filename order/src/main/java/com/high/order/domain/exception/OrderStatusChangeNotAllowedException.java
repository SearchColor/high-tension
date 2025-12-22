package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderStatusChangeNotAllowedException extends CustomException {
    public OrderStatusChangeNotAllowedException() {
        super(OrderErrorCode.ORDER_STATUS_CHANGE_NOT_ALLOWED);
    }
}
