package com.high.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원정보 수정 요청 (변경할 필드만 전송)")
public record UpdateUserRequest(
    @Schema(description = "사용자 이름 (최대 50자)", example = "홍길동")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    String name,

    @Schema(description = "전화번호 (형식: 010-1234-5678)", example = "010-9876-5432")
    @Pattern(regexp = "^(010)(-?\\d{4})(-?\\d{4})$",
             message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    String phoneNumber,

    @Schema(description = "배송 주소 (최대 255자)", example = "서울시 강남구 역삼로 456")
    @Size(max = 255, message = "배송 주소는 255자 이하여야 합니다")
    String deliveryAddress,

    @Schema(description = "상세 주소 (최대 255자)", example = "202동 202호")
    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다")
    String detailAddress
) {
    // 최소 1개 필드는 null이 아니어야 함 (Service에서 검증)
}
