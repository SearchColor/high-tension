package com.high.order.infrastructure.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class FeignCommunicationErrorException extends CustomException {

    public FeignCommunicationErrorException() {
        super(OrderErrorCode.FEIGN_COMMUNICATION_ERROR);
    }
}
