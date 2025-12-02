package com.high.order.domain.exception;

import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidOrderStateException extends CustomException {

    private static final int INVALID_ORDER_STATUS = 4003;

    public InvalidOrderStateException() {
        super(HttpStatus.BAD_REQUEST, INVALID_ORDER_STATUS, "주문 상태를 변경할 수 없습니다.");
    }
}
