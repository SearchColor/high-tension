package com.high.user.application.dto.response;

import com.high.user.domain.entity.User;
import com.high.user.domain.vo.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        String name,
        UserRole role,
        String phoneNumber,
        String deliveryAddress,
        String detailAddress,
        Boolean isActive,
        LocalDateTime lastLoginAt,
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
