package com.high.user.application.service;

import com.high.user.application.dto.response.InternalUserResponse;
import com.high.user.domain.entity.User;
import com.high.user.domain.exception.UserNotFoundException;
import com.high.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 내부 서비스 간 통신용 사용자 조회 Service
 * <p>
 * Order/Payment/Coupon Service에서 User 정보를 조회할 때 사용
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalUserService {

    private final UserRepository userRepository;

    /**
     * 단건 조회 - Order/Payment/Coupon Service에서 사용
     *
     * @param userId 사용자 고유 ID
     * @return InternalUserResponse
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     */
    public InternalUserResponse getUserById(UUID userId) {
        log.info("[Internal API] Fetching user with id: {}", userId);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> {
                    log.warn("[Internal API] User not found with id: {}", userId);
                    return new UserNotFoundException();
                });

        return InternalUserResponse.from(user);
    }

    /**
     * 다건 조회 - Batch 조회 (Order Service 등에서 사용)
     *
     * @param userIds 사용자 ID 목록
     * @return InternalUserResponse 리스트
     */
    public List<InternalUserResponse> getUsersByIds(List<UUID> userIds) {
        log.info("[Internal API] Fetching users with ids: {} (count: {})", userIds, userIds.size());

        List<User> users = userRepository.findByUserIdInAndDeletedAtIsNull(userIds);

        log.info("[Internal API] Found {} users out of {} requested", users.size(), userIds.size());

        return users.stream()
                .map(InternalUserResponse::from)
                .toList();
    }
}
