package com.high.order.domain.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class DeliveryStatusChangeNotAllowedException extends CustomException {

    public DeliveryStatusChangeNotAllowedException() {
        super(OrderErrorCode.DELIVERY_STATUS_CHANGE_NOT_ALLOWED);
    }
}
