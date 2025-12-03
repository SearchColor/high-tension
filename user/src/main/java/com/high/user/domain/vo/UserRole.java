package com.high.user.domain.vo;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("일반 사용자"),
    SELLER("판매자"),
    MASTER("관리자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    /**
     * 권한 레벨 비교
     * MASTER > SELLER > USER
     */
    public boolean hasPermission(UserRole required) {
        return this.ordinal() >= required.ordinal();
    }

    /**
     * String으로부터 UserRole 변환
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}