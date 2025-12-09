package com.high.user.application.service;

import com.high.user.application.dto.request.ChangePasswordRequest;
import com.high.user.application.dto.request.DeleteUserRequest;
import com.high.user.application.dto.request.UpdateUserRequest;
import com.high.user.application.dto.response.UserResponse;
import com.high.user.domain.entity.User;
import com.high.user.domain.exception.*;
import com.high.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    /**
     * 사용자 ID로 사용자 정보 조회 (읽기 전용)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        UUID userUuid;

        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for userId: {}", userId);
            throw new IllegalArgumentException("Invalid user ID format", e);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userUuid)
                .orElseThrow(() -> {
                    log.warn("User not found or deleted: userId={}", userId);
                    return new UserNotFoundException();
                });

        log.info("User info retrieved successfully: userId={}, email={}", user.getUserId(), user.getEmail());

        return UserResponse.from(user);
    }

    /**
     * 사용자 정보 수정
     *
     * @param userId  사용자 ID (SecurityContext에서 추출)
     * @param request 수정할 사용자 정보
     * @return UserResponse
     */
    @Transactional
    public UserResponse updateUserInfo(UUID userId, UpdateUserRequest request) {
        // 최소 1개 필드 검증
        if (request.name() == null && request.phoneNumber() == null &&
            request.deliveryAddress() == null && request.detailAddress() == null) {
            throw new IllegalArgumentException("수정할 필드가 없습니다");
        }

        // 사용자 조회 (deletedAt이 null인 사용자만)
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(UserNotFoundException::new);

        // 계정 활성화 상태 확인
        if (!user.getIsActive()) {
            throw new InactiveAccountException();
        }

        // 선택적 업데이트 (null이 아닌 필드만)
        if (request.name() != null) {
            user.updateName(request.name());
        }
        if (request.phoneNumber() != null) {
            user.updatePhoneNumber(request.phoneNumber());
        }
        if (request.deliveryAddress() != null || request.detailAddress() != null) {
            user.updateAddress(request.deliveryAddress(), request.detailAddress());
        }

        log.info("사용자 정보 수정 완료 - userId: {}", userId);
        return UserResponse.from(user);
    }

    /**
     * 비밀번호 변경
     *
     * @param userId  사용자 ID (SecurityContext에서 추출)
     * @param request 현재 비밀번호 및 새 비밀번호
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        // 사용자 조회
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(UserNotFoundException::new);

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 새 비밀번호가 현재 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        // 비밀번호 암호화 및 업데이트
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(encodedPassword);

        log.info("비밀번호 변경 완료 - userId: {}", userId);
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     *
     * @param userId  사용자 ID (SecurityContext에서 추출)
     * @param request 비밀번호 재확인
     */
    @Transactional
    public void deleteUser(UUID userId, DeleteUserRequest request) {
        // 사용자 조회
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(UserNotFoundException::new);

        // 이미 탈퇴한 계정인지 확인
        if (user.getDeletedAt() != null) {
            throw new AlreadyDeletedException();
        }

        // 비밀번호 재확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Soft Delete
        user.softDelete(userId);

        // Refresh Token 삭제
        refreshTokenService.deleteByUserId(userId.toString());

        log.info("회원 탈퇴 완료 - userId: {}", userId);
    }
}
