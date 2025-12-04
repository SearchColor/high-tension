package com.high.product.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCreateRequest(
	@NotBlank(message = "상품 이름은 필수입니다.")
	String name,

	@NotNull(message = "상품 가격은 필수입니다.")
	@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
	Integer price,

	@NotBlank(message = "카테고리는 필수입니다.")
	String category,

	@NotBlank(message = "판매자 정보는 필수입니다.")
	String seller
) {
}
