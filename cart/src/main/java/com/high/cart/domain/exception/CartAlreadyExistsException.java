package com.high.cart.domain.exception;

import com.high.cart.exception.CartErrorCode;
import com.library.module.exception.CustomException;

public class CartAlreadyExistsException extends CustomException {

    public CartAlreadyExistsException() {
        super(CartErrorCode.CONFLICT);
    }
}

