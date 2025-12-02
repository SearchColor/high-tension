package com.high.order.domain.exception;

import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OrderCancellationException extends CustomException {

    private static final int ORDER_CANCELLATION_NOT_ALLOWED = 4004;


    public OrderCancellationException() {
        super(HttpStatus.BAD_REQUEST, ORDER_CANCELLATION_NOT_ALLOWED, "주문을 취소할 수 없습니다.");
    }
}
