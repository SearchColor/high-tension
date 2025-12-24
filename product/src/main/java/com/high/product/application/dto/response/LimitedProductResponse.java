package com.high.product.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.high.product.domain.model.Limited_Product;

import io.swagger.v3.oas.annotations.media.Schema;

// 한정상품 조회에 사용되는 Dto
@Schema(description = "한정상품 조회 응답 DTO")
public record LimitedProductResponse(

	UUID id,

	String name,

	int price,

	String category,

	UUID seller,

	int discountRate,

	LocalDateTime endTime,

	LocalDateTime createdAt,

	UUID createBy
) {
	public static LimitedProductResponse from(Limited_Product limittedProduct) {
		return new LimitedProductResponse(
			limittedProduct.getId(),
			limittedProduct.getName(),
			limittedProduct.getPrice(),
			limittedProduct.getCategory(),
			limittedProduct.getSeller(),
			limittedProduct.getDiscountrate(),
			limittedProduct.getEnd(),
			limittedProduct.getCreatedAt(),
			limittedProduct.getCreatedBy()
		);
	}
}
