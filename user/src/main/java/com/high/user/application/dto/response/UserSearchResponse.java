package com.high.user.application.dto.response;

import com.high.user.domain.entity.User;
import com.high.user.domain.vo.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSearchResponse(
        UUID userId,
        String email,
        String name,
        UserRole role,
        Boolean isActive,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}