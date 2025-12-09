package com.high.product.application.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LimitedProductCreateRequest(

	@NotBlank(message = "이름은 필수입니다.")
	String name,

	@NotNull(message = "가격은 필수입니다.")
	@Min(0)
	int price,

	@NotBlank(message = "카테고리는 필수입니다.")
	String category,

	@NotNull(message = "판매자는 필수입니다.")
	UUID seller,

	@NotNull(message = "할인율은 필수입니다.")
	@Min(0)
	@Max(100)
	int discountRate,

	@NotNull(message = "종료일(end)은 필수입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime end
) {
}
