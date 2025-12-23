package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderCancellationNotAllowedByItemStatusException extends CustomException {

    public OrderCancellationNotAllowedByItemStatusException() {
        super(OrderErrorCode.ORDER_CANCELLATION_NOT_ALLOWED_BY_ITEM_STATUS);
    }
}
