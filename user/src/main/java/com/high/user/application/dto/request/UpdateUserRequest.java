package com.high.user.application.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    String name,

    @Pattern(regexp = "^(010)(-?\\d{4})(-?\\d{4})$",
             message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    String phoneNumber,

    @Size(max = 255, message = "배송 주소는 255자 이하여야 합니다")
    String deliveryAddress,

    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다")
    String detailAddress
) {
    // 최소 1개 필드는 null이 아니어야 함 (Service에서 검증)
}
