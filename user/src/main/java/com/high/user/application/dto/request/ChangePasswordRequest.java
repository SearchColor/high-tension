package com.high.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 변경 요청")
public record ChangePasswordRequest(
    @Schema(description = "현재 비밀번호", example = "OldPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "현재 비밀번호를 입력해주세요")
    String currentPassword,

    @Schema(description = "새 비밀번호 (8자 이상, 영문+숫자+특수문자)", example = "NewPass456!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새 비밀번호를 입력해주세요")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
    String newPassword
) {}
