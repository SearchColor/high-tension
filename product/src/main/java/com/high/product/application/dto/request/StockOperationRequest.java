package com.high.product.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 재고 증감 요청 DTO (입고/출고/복원 시)
@Schema(description = "재고 증감 요청 DTO")
public record StockOperationRequest(
	@NotNull(message = "수량은 필수입니다.")
	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	Integer amount
) {}
