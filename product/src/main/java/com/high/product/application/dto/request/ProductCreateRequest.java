package com.high.product.application.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "일반상품 생성 요청 DTO")
public record ProductCreateRequest(

	@Schema(description = "일반상품 이름", example = "꿀잠을 잘 수 있는 수면밴드")
	@NotBlank(message = "상품 이름은 필수입니다.")
	String name,

	@Schema(description = "일반상품 가격", example = "8000")
	@NotNull(message = "상품 가격은 필수입니다.")
	@Min(value = 0, message = "가격은 0 이상이어야 합니다.")
	Integer price,

	@Schema(description = "일반상품 카테고리", example = "생활용품")
	@NotBlank(message = "카테고리는 필수입니다.")
	String category,

	@Schema(description = "일반상품 판매자", example = "654a7d24-6c17-4cb9-9766-b46e8043037e")
	@NotNull(message = "판매자 정보는 필수입니다.")
	UUID seller
) {
}
