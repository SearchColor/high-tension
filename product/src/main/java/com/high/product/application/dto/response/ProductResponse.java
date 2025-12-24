package com.high.product.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.high.product.domain.model.Product;

import io.swagger.v3.oas.annotations.media.Schema;

// Id 단건 조회, 페이징 & 정렬 적용한 전체 조회에 사용하는 DTO
@Schema(description = "일반상품 조회 응답 DTO")
public record ProductResponse(
	UUID id,

	String name,

	int price,

	String category,

	UUID seller,

	LocalDateTime createdAt,

	UUID createdBy,

	LocalDateTime updatedAt,

	UUID updatedBy,

	LocalDateTime deletedAt,

	UUID deletedBy
) {
	public static ProductResponse from(Product product) {
		return new ProductResponse(
			product.getId(),
			product.getName(),
			product.getPrice(),
			product.getCategory(),
			product.getSeller(),
			product.getCreatedAt(),
			product.getCreatedBy(),
			product.getUpdatedAt(),
			product.getUpdatedBy(),
			product.getDeletedAt(),
			product.getDeletedBy()
		);
	}
}
