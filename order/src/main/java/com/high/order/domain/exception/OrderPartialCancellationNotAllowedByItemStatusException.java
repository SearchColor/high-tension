package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class OrderPartialCancellationNotAllowedByItemStatusException extends CustomException {

    public OrderPartialCancellationNotAllowedByItemStatusException() {
        super(OrderErrorCode.ORDER_PARTIAL_CANCELLATION_NOT_ALLOWED_BY_ITEM_STATUS);
    }
}
