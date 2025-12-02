package com.high.order.application.exception;

import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OrderBadRequestException extends CustomException {

    private static final int BAD_REQUEST = 4001;

    public OrderBadRequestException() {
        super(HttpStatus.BAD_REQUEST, BAD_REQUEST, "잘못된 요청입니다.");
    }
}
