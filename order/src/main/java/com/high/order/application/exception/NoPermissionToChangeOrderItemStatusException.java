package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class NoPermissionToChangeOrderItemStatusException extends CustomException {

    public NoPermissionToChangeOrderItemStatusException() {
        super(OrderErrorCode.NO_PERMISSION_TO_CHANGE_ORDER_ITEM_STATUS);
    }
}
