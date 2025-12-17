package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class NoPermissionToCancelOrderException extends CustomException {

    public NoPermissionToCancelOrderException() {
        super(OrderErrorCode.NO_PERMISSION_TO_CANCEL_ORDER);
    }
}
