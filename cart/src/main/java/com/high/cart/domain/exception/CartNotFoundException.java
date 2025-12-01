package com.high.cart.domain.exception;

import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CartNotFoundException extends CustomException {

    private static final int CART_NOT_FOUND_ERROR_CODE = 404101;

    public CartNotFoundException(String userId) {
        super(
                // 1. HTTP 상태 코드
                HttpStatus.NOT_FOUND,
                // 2. 내부 에러 코드
                CART_NOT_FOUND_ERROR_CODE,
                // 3. 에러 메시지
                "Cart not found with userId : " + userId
        );
    }
}
