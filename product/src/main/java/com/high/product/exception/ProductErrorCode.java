package com.high.product.exception;

import org.springframework.http.HttpStatus;

import com.library.module.exception.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements BaseErrorCode {

	// 일반&한정상품 관련 에러코드
	PRODUCT_NOT_FOUND(3000, HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
	DUPLICATE_PRODUCT_NAME(3001, HttpStatus.CONFLICT, "이미 존재하는 상품명입니다."),
	INVALID_PRODUCT_PRICE(3002, HttpStatus.BAD_REQUEST, "가격이 유효하지 않습니다."),
	INVALID_CATEGORY(3003, HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),
	INVALID_SELLER(3004, HttpStatus.FORBIDDEN, "해당 상품에 접근할 권한이 없습니다."),

	// 재고 관련 에러코드
	STOCK_NOT_FOUND(3100, HttpStatus.NOT_FOUND, "해당 재고 정보를 찾을 수 없습니다."),
	INSUFFICIENT_STOCK(3101, HttpStatus.BAD_REQUEST, "재고가 부족하여 요청을 처리할 수 없습니다."),
	NEGATIVE_STOCK_OPERATION(3102, HttpStatus.BAD_REQUEST, "재고 증감량은 0보다 커야 합니다."),
	DUPLICATE_STOCK(3103, HttpStatus.CONFLICT, "이미 해당 상품에 재고가 존재합니다."),
	STOCK_QUANTITY_BELOW_ZERO(3104, HttpStatus.BAD_REQUEST, "재고 수량은 0 미만이 될 수 없습니다."),
	PRODUCT_ALREADY_DELETED(3105, HttpStatus.BAD_REQUEST, "올바른 상품이 아니므로 다른 상품의 재고관련 요청 부탁드립니다."),

	// 한정상품 관련 에러코드
	LIMITTED_PRODUCT_NOT_FOUND(3200, HttpStatus.BAD_REQUEST, "할인율은 0% 미만이거나 100% 넘게 초과될 수 없습니다.");

	private final int code;
	private final HttpStatus status;
	private final String message;

}

