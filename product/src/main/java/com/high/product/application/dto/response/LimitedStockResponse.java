package com.high.product.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.high.product.domain.model.Limited_Product_Stock;

public record LimitedStockResponse(
	UUID id,

	UUID limitedProductId,

	int quantity,

	LocalDateTime createdAt,

	UUID createdBy
) {
	public static LimitedStockResponse from(Limited_Product_Stock limitedProductStock) {
		return new LimitedStockResponse(
			limitedProductStock.getId(),
			limitedProductStock.getLimitedProductId(),
			limitedProductStock.getQuantity(),
			limitedProductStock.getCreatedAt(),
			limitedProductStock.getCreatedBy()
		);
	}
}
