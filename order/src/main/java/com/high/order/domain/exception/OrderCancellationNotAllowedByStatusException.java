package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderCancellationNotAllowedByStatusException extends CustomException {

    public OrderCancellationNotAllowedByStatusException() {
        super(OrderErrorCode.ORDER_CANCELLATION_NOT_ALLOWED_BY_STATUS);
    }
}
