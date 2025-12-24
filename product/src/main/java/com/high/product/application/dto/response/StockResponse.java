package com.high.product.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.high.product.domain.model.Product_Stock;

import io.swagger.v3.oas.annotations.media.Schema;

// // 재고 단건 조회, 재고 전체 조회에 사용하는 DTO
@Schema(description = "일반상품 재고 조회 응답 DTO")
public record StockResponse(
	UUID id,

	UUID productId,

	int quantity,

	LocalDateTime createdAt,

	UUID createdBy,

	LocalDateTime updatedAt,

	UUID updatedBy,

	LocalDateTime deletedAt,

	UUID deletedBy
) {
	public static StockResponse from(Product_Stock stock) {
		return new StockResponse(
			stock.getId(),
			stock.getProductId(),
			stock.getQuantity(),
			stock.getCreatedAt(),
			stock.getCreatedBy(),
			stock.getUpdatedAt(),
			stock.getUpdatedBy(),
			stock.getDeletedAt(),
			stock.getDeletedBy()
		);
	}
}