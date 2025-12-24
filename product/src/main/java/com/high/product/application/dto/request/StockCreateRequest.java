package com.high.product.application.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "일반상품 재고 생성 요청 DTO")
public record StockCreateRequest(

	@Schema(description = "일반상품 ID", example = "c166a100-ee5c-4fed-817a-eef734128257")
	@NotNull(message = "상품 ID는 필수입니다.")
	UUID productId,

	@Schema(description = "일반상품 수량", example = "100")
	@NotNull(message = "재고 수량은 필수입니다.")
	@Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
	Integer quantity
) {}
