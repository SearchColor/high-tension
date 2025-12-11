package com.high.payment.exception;

import org.springframework.http.HttpStatus;

import com.library.module.exception.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {
	// Application Exception
	PAYMENT_BAD_REQUEST(5000, HttpStatus.BAD_REQUEST, "잘못된 결제 요청입니다."),

	// Domain Exception
	PAYMENT_NOT_FOUND(5001, HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
	INVALID_PAYMENT_STATUS(5002, HttpStatus.BAD_REQUEST, "현재 상태에서는 결제 상태를 변경할 수 없습니다."),
	PAYMENT_VERIFICATION_FAILED(5003, HttpStatus.BAD_REQUEST, "결제 정보 검증(금액 불일치)에 실패했습니다."),
	PAYMENT_CANCELLATION_NOT_ALLOWED(5004, HttpStatus.BAD_REQUEST, "결제 취소가 허용되지 않는 상태입니다."),

	// PG 연동 Exception
	PG_CLIENT_ERROR(5005, HttpStatus.SERVICE_UNAVAILABLE, "PG사 연동 중 오류가 발생했습니다."),

	PAYMENT_INTERNAL_SERVER_ERROR(5006, HttpStatus.INTERNAL_SERVER_ERROR, "결제 서비스 내부 오류가 발생했습니다.");

	private final int code;
	private final HttpStatus status;
	private final String message;
}
