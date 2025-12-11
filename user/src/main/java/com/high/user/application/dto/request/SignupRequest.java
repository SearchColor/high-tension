package com.high.user.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다"
    )
    String password,

    @NotBlank(message = "이름은 필수입니다")
    String name,

    @Pattern(
          regexp = "^(010)(-?\\d{4})(-?\\d{4})$",
          message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"
    ) String phoneNumber,

    @Size(max = 255, message = "배송 주소는 255자 이하여야 합니다")
    String deliveryAddress,

    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다")
    String detailAddress
) {
}
