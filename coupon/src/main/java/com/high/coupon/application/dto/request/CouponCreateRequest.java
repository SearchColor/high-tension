package com.high.coupon.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "쿠폰 생성 요청 DTO")
public record CouponCreateRequest(

        @Schema(description = "쿠폰 이름", example = "전상품 20% 할인 쿠폰")
        @NotBlank(message = "쿠폰 이름을 입력해주세요.")
        String name,

        @Schema(description = "쿠폰 설명", example = "전상품 20% 할인이 적용됩니다.")
        @NotBlank(message = "쿠폰 정책(설명)을 입력해주세요.")
        String description,


        @Schema(description = "할인율(%)", example = "20", minimum = "1", maximum = "100")
        @NotNull(message = "할인율을 입력해주세요")
        @DecimalMin(value = "1", inclusive = true, message = "할인율은 1% 이상이어야 합니다.")
        @DecimalMax(value = "100", inclusive = true, message = "할인율은 100% 이하여야 합니다.")
        BigDecimal discountRate,

        @Schema(description = "쿠폰 총 발행 수량", example = "1000", minimum = "0")
        @NotNull(message = "총 발행 수량을 입력해주세요.")
        @PositiveOrZero(message = "총 발행 수량은 0 이상이어야 합니다.")
        Integer totalQuantity,

        @Schema(description = "쿠폰 발행 시작 일시", example = "2026-01-01 00:00:00", type = "string", format = "date-time")
        @NotNull(message = "발행 시작일을 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime issueStartAt,

        @Schema(description = "쿠폰 발행 종료 일시", example = "2026-01-10 00:00:00", type = "string", format = "date-time")
        @NotNull(message = "발행 종료일을 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime issueEndAt,

        @Schema(description = "쿠폰 사용 가능 만료 일시", example = "2026-01-10 00:00:00", type = "string", format = "date-time")
        @NotNull(message = "쿠폰 만료일을 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime validUntil
) {
}
