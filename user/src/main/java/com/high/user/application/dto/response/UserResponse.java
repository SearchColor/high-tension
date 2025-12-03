package com.high.user.application.dto.response;

import com.high.user.domain.entity.User;
import com.high.user.domain.vo.UserRole;

import java.util.UUID;

public record UserResponse(
    UUID userId,
    String email,
    String name,
    UserRole role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getUserId(),
            user.getEmail(),
            user.getName(),
            user.getRole()
        );
    }
}
