package com.high.product.exception;

import com.library.module.exception.BaseErrorCode;
import com.library.module.exception.CustomException;

public class InsufficientStockException extends CustomException {
	public InsufficientStockException(BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}
}
