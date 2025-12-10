package com.high.user.application.service;

import com.high.user.application.dto.request.UpdateRoleRequest;
import com.high.user.application.dto.response.UserSearchResponse;
import com.high.user.domain.entity.User;
import com.high.user.domain.exception.UserNotFoundException;
import com.high.user.domain.exception.UserRoleChangeNotAllowedException;
import com.high.user.domain.repository.UserRepository;
import com.high.user.domain.vo.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자 검색
     *
     * @param email 검색할 이메일 (부분 일치)
     * @param page  페이지 번호
     * @param size  페이지 크기
     * @return 검색된 사용자 목록
     */
    public Page<UserSearchResponse> searchUsersByEmail(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findByEmailContainingAndDeletedAtIsNull(email, pageable);

        log.info("[ADMIN] Searched users by email: email={}, resultCount={}", email, users.getTotalElements());

        return users.map(UserSearchResponse::from);
    }

    /**
     * 사용자 권한 변경
     *
     * @param userId  대상 사용자 ID
     * @param request 변경할 권한
     * @return 변경된 사용자 정보
     */
    @Transactional
    public UserSearchResponse updateUserRole(UUID userId, UpdateRoleRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);

        // MASTER 권한 변경 불가
        if (user.getRole() == UserRole.MASTER) {
            throw new UserRoleChangeNotAllowedException();
        }

        // MASTER로 변경 불가 (MASTER는 직접 생성만 가능)
        if (request.role() == UserRole.MASTER) {
            throw new UserRoleChangeNotAllowedException();
        }

        user.updateRole(request.role());

        log.info("[ADMIN] Updated user role: userId={}, newRole={}", userId, request.role());

        return UserSearchResponse.from(user);
    }
}
