package com.high.user.application.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * 다건 사용자 조회 Request DTO
 * <p>
 * Internal API에서 여러 사용자의 정보를 한 번에 조회할 때 사용
 * </p>
 *
 * @param userIds 조회할 사용자 ID 목록
 */
public record BatchUserRequest(
        @NotEmpty(message = "사용자 ID 목록은 필수입니다")
        List<UUID> userIds
) {
}
