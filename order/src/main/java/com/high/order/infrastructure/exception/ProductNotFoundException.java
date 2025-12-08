package com.high.order.infrastructure.exception;

import com.high.order.exception.OrderErrorCode;
import com.library.module.exception.CustomException;

public class ProductNotFoundException extends CustomException {

    public ProductNotFoundException() {
        super(OrderErrorCode.PRODUCT_NOT_FOUND);
    }
}
