package com.high.user.application.service;

import com.high.user.application.dto.response.UserResponse;
import com.high.user.domain.entity.User;
import com.high.user.domain.exception.UserNotFoundException;
import com.high.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * User Query Service
 *
 * 사용자 조회 전용 서비스
 * - 인증과 무관한 순수 조회 로직만 포함
 * - CQRS 패턴의 Query 측면 담당
 * - 모든 조회 메서드는 @Transactional(readOnly = true) 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 사용자 정보 조회 (읽기 전용)
     *
     * @param userId 조회할 사용자 ID (String 형태의 UUID)
     * @return UserResponse 사용자 정보 응답 DTO
     * @throws UserNotFoundException 사용자를 찾을 수 없거나 삭제된 경우
     * @throws IllegalArgumentException userId가 유효하지 않은 UUID 형식인 경우
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
     * 향후 확장 가능 메서드 예시:
     * - getUsersByRole(UserRole role)
     * - searchUsersByEmail(String emailPattern)
     * - getUserProfile(UUID userId) - 주소, 전화번호 등 상세 정보 포함
     */
}