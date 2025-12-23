package com.high.payment.application.dto.external;

public record ApiResponse<T>(
	boolean success,
	T data,
	String message
) {}
