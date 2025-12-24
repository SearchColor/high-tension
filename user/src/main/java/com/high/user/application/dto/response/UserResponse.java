package com.high.user.application.dto.response;

import com.high.user.domain.entity.User;
import com.high.user.domain.vo.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "사용자 정보 응답")
public record UserResponse(
        @Schema(description = "사용자 고유 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "권한 (USER, SELLER, MASTER)", example = "USER")
        UserRole role,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123")
        String deliveryAddress,

        @Schema(description = "상세 주소", example = "101동 101호")
        String detailAddress,

        @Schema(description = "활성화 상태", example = "true")
        Boolean isActive,

        @Schema(description = "마지막 로그인 시간", example = "2025-01-15T10:30:00")
        LocalDateTime lastLoginAt,

        @Schema(description = "가입일시", example = "2025-01-01T09:00:00")
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getDeliveryAddress(),
                user.getDetailAddress(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
