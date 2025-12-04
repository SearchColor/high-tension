package com.high.product.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LimittedStockCreateRequest(

	@NotNull
	UUID limittedProductId,

	@NotNull
	@Min(0)
	int quantity
){}
