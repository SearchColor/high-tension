package com.high.order.application.exception;

import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OrderItemNotFoundExeption extends CustomException {

    private static final int ORDER_ITEM_NOT_FOUND_CODE = 4002;

    public OrderItemNotFoundExeption() {
        super(HttpStatus.NOT_FOUND, ORDER_ITEM_NOT_FOUND_CODE, "주문 상품을 찾을 수 없습니다.");
    }
}
