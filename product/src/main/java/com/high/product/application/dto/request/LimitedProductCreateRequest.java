package com.high.product.application.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "한정상품 생성 요청 DTO")
public record LimitedProductCreateRequest(

	@Schema(description = "한정상품 이름", example = "100개 한정수량, 맛있는 초콜릿")
	@NotBlank(message = "이름은 필수입니다.")
	String name,

	@Schema(description = "한정상품 가격", example = "1000")
	@NotNull(message = "가격은 필수입니다.")
	@Min(0)
	int price,

	@Schema(description = "한정상품 카테고리", example = "간식")
	@NotBlank(message = "카테고리는 필수입니다.")
	String category,

	@Schema(description = "한정상품 판매자", example = "박춘봉")
	@NotNull(message = "판매자는 필수입니다.")
	UUID seller,

	@Schema(description = "한정상품 할인율", example = "90")
	@NotNull(message = "할인율은 필수입니다.")
	@Min(0)
	@Max(100)
	int discountRate,

	@Schema(description = "한정상품 판매 종료일", example = "2025-12-31T10:00:00")
	@NotNull(message = "종료일(end)은 필수입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime end
) {
}
