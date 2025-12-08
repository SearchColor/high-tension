package com.high.payment.exception;

import com.library.module.exception.BaseErrorCode;

public class PaymentException extends RuntimeException {
	private final BaseErrorCode errorCode;

	public PaymentException(BaseErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public PaymentException(BaseErrorCode errorCode, String detailMessage) {
		super(detailMessage);
		this.errorCode = errorCode;
	}

	public BaseErrorCode getErrorCode() {
		return errorCode;
	}
}
