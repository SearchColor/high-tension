package com.high.order.infrastructure.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class EmptyKafkaMessageException extends CustomException {


    public EmptyKafkaMessageException() {
        super(OrderErrorCode.EMPTY_KAFKA_MESSAGE);
    }
}
