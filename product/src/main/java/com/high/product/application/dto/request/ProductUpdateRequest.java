package com.high.product.application.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "일반상품 수정 요청 DTO")
public record ProductUpdateRequest(

	@Schema(description = "일반상품 이름", example = "건강하고 맛있는 우유1L 3개")
	String name,

	@Schema(description = "일반상품 가격", example = "15000")
	@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
	Integer price,

	@Schema(description = "일반상품 카테고리", example = "꿀잠을 잘 수 있는 수면밴드")
	String category,

	@Schema(description = "일반상품 판매자", example = "2f8e7d24-cc17-1cb9-i766-a46e80d3037e")
	UUID seller
) {
}
