package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class DeliveryStatusChangeNotAllowedException extends CustomException {

    public DeliveryStatusChangeNotAllowedException() {
        super(OrderErrorCode.DELIVERY_STATUS_CHANGE_NOT_ALLOWED);
    }
}
