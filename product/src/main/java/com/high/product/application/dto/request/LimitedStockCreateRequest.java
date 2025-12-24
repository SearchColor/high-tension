package com.high.product.application.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "한정상품 재고 생성 요청 DTO")
public record LimitedStockCreateRequest(

	@Schema(description = "한정상품 ID", example = "469f2887-afb2-4c7b-a89e-848d5644994e")
	@NotNull
	UUID limitedProductId,

	@Schema(description = "한정상품 수량", example = "100")
	@NotNull
	@Min(0)
	int quantity
){}
