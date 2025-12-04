package com.high.product.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.high.product.domain.model.Limitted_Product_Stock;

public record LimittedStockResponse(
	UUID id,

	UUID limitedProductId,

	int quantity,

	LocalDateTime createdAt,

	UUID createdBy
) {
	public static LimittedStockResponse from(Limitted_Product_Stock limittedProductStock) {
		return new LimittedStockResponse(
			limittedProductStock.getId(),
			limittedProductStock.getLimittedProductId(),
			limittedProductStock.getQuantity(),
			limittedProductStock.getCreatedAt(),
			limittedProductStock.getCreatedBy()
		);
	}
}
