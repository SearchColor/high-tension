package com.high.user.application.dto.request;

import com.high.user.domain.vo.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "변경할 권한은 필수입니다")
        UserRole role
) {
}