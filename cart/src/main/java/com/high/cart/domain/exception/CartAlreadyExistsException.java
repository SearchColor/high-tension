package com.high.cart.domain.exception;

import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CartAlreadyExistsException extends CustomException {

    private static final int CART_ALREADY_EXISTS_ERROR_CODE = 404109;

    public CartAlreadyExistsException(String userId) {
        super(
                // 1. HTTP 상태 코드
                HttpStatus.CONFLICT,
                // 2. 내부 에러 코드
                CART_ALREADY_EXISTS_ERROR_CODE,
                // 3. 에러 메시지
                "Cart already exists for user: " + userId
        );
    }
}

