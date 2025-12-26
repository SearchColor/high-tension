package com.high.order.application.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class IllegalContactFormatRequestException extends CustomException {


    public IllegalContactFormatRequestException() {
        super(OrderErrorCode.ILLEGAL_CONTACT_FORMAT_REQUEST);
    }
}
