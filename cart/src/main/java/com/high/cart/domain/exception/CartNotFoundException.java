package com.high.cart.domain.exception;

import com.high.cart.exception.CartErrorCode;
import com.library.module.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CartNotFoundException extends CustomException {

    public CartNotFoundException() {
        super(CartErrorCode.NOT_FOUND);
    }
}
