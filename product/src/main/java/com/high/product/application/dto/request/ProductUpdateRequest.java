package com.high.product.application.dto.request;

import jakarta.validation.constraints.Min;

public record ProductUpdateRequest(

	String name,

	@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
	Integer price,

	String category,

	String seller
) {
}
