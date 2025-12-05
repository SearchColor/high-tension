package com.high.product.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LimitedStockCreateRequest(

	@NotNull
	UUID limitedProductId,

	@NotNull
	@Min(0)
	int quantity
){}
