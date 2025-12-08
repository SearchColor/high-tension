package com.high.user.domain.entity;

import com.high.user.domain.vo.UserRole;
import com.library.jpa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String deliveryAddress;

    @Column(length = 255)
    private String detailAddress;

    @Column(nullable = false)
    private Boolean isActive;

    @Column
    private LocalDateTime lastLoginAt;

    // Private constructor for factory methods
    private User(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.isActive = true;
    }

    // Factory method: 일반 회원가입 (LOCAL 사용자)
    public static User createLocalUser(String email, String encodedPassword, String name) {
        validateEmail(email);
        validateName(name);

        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        return new User(email, encodedPassword, name, UserRole.USER);
    }

    // Factory method: 관리자 생성 (MASTER)
    public static User createMasterUser(String email, String encodedPassword, String name) {
        validateEmail(email);
        validateName(name);

        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        return new User(email, encodedPassword, name, UserRole.MASTER);
    }

    // Business logic: 이메일 수정
    public void updateEmail(String newEmail) {
        validateEmail(newEmail);
        this.email = newEmail;
    }

    // Business logic: 비밀번호 변경
    public void updatePassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        this.password = encodedPassword;
    }

    // Business logic: 이름 수정
    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    // Business logic: 전화번호 수정
    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.matches("^(010)(-?\\d{4})(-?\\d{4})$")) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다");
        }
        this.phoneNumber = phoneNumber;
    }

    // Business logic: 주소 수정
    public void updateAddress(String deliveryAddress, String detailAddress) {
        this.deliveryAddress = deliveryAddress;
        this.detailAddress = detailAddress;
    }

    // Business logic: 권한 변경 (MASTER만 변경 불가)
    public void updateRole(UserRole newRole) {
        if (this.role == UserRole.MASTER) {
            throw new IllegalStateException("MASTER 권한은 변경할 수 없습니다");
        }
        this.role = newRole;
    }

    // Business logic: 마지막 로그인 시간 업데이트
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // Business logic: 계정 활성화
    public void activate() {
        this.isActive = true;
    }

    // Business logic: 계정 비활성화
    public void deactivate() {
        this.isActive = false;
    }

    // Business logic: Soft Delete
    public void softDelete(String deletedBy) {
        super.softDelete(deletedBy);
        this.isActive = false;
    }

    // Query method: 계정이 활성화 되어 있고 삭제되지 않았는지 확인
    public boolean isActiveAndNotDeleted() {
        return this.isActive && !isDeleted();
    }

    // Query method: 권한 확인
    public boolean hasRole(UserRole requiredRole) {
        return this.role.hasPermission(requiredRole);
    }

    // Validation: 이메일 형식 검증
    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Validation: 이름 검증
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Name must be 50 characters or less");
        }
    }
}