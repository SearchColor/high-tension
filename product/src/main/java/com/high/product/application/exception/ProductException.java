package com.high.product.application.exception;

import com.library.module.exception.BaseErrorCode;
import com.library.module.exception.CustomException;

public class ProductException extends CustomException {

	public ProductException(BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}
}
