package com.high.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(

    @Schema(description = "이메일 (로그인 ID)", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @Schema(description = "비밀번호 (8자 이상, 영문+숫자+특수문자)", example = "Pass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다"
    )
    String password,

    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    String name,

    @Schema(description = "전화번호 (형식: 010-1234-5678)", example = "010-1234-5678")
    @Pattern(
          regexp = "^(010)(-?\\d{4})(-?\\d{4})$",
          message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"
    ) String phoneNumber,

    @Schema(description = "배송 주소 (최대 255자)", example = "서울시 강남구 테헤란로 123")
    @Size(max = 255, message = "배송 주소는 255자 이하여야 합니다")
    String deliveryAddress,

    @Schema(description = "상세 주소 (최대 255자)", example = "101동 101호")
    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다")
    String detailAddress
) {
}
