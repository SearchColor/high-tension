package com.high.coupon.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponCreateRequest(

        @NotBlank(message = "쿠폰 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "쿠폰 정책(설명)을 입력해주세요.")
        String description,

        @NotNull(message = "할인율을 입력해주세요")
        @DecimalMin(value = "1", inclusive = true, message = "할인율은 1% 이상이어야 합니다.")
        @DecimalMax(value = "100", inclusive = true, message = "할인율은 100% 이하여야 합니다.")
        BigDecimal discountRate,

        @NotNull(message = "총 발행 수량을 입력해주세요.")
        @PositiveOrZero(message = "총 발행 수량은 0 이상이어야 합니다.")
        Integer totalQuantity,

        @NotNull(message = "발행 시작일을 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime issueStartAt,

        @NotNull(message = "발행 종료일을 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime issueEndAt,

        @NotNull(message = "쿠폰 만료일을 입력해주세요.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime validUntil
) {
}
