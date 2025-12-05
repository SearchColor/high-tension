package com.high.product.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockCreateRequest(
	@NotNull(message = "상품 ID는 필수입니다.")
	UUID productId,

	@NotNull(message = "재고 수량은 필수입니다.")
	@Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
	Integer quantity
) {}
