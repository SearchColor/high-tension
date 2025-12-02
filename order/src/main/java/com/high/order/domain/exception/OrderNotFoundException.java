package com.high.order.domain.exception;

import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends CustomException {

    private static final int ORDER_NOT_FOUND_CODE = 4000;

    public OrderNotFoundException() {
        super(HttpStatus.NOT_FOUND,
              ORDER_NOT_FOUND_CODE,
            "주문을 찾을 수 없습니다.");
    }
}
