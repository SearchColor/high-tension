package com.high.product.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.high.product.domain.model.Limitted_Product;

// 한정상품 조회에 사용되는 Dto
public record LimittedProductResponse(

	UUID id,

	String name,

	int price,

	String category,

	String seller,

	int discountRate,

	LocalDateTime endTime,

	LocalDateTime createdAt,

	UUID createBy
) {
	public static LimittedProductResponse from(Limitted_Product limittedProduct) {
		return new LimittedProductResponse(
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
