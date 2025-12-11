package com.high.user.presentation;

import com.high.user.application.dto.request.BatchUserRequest;
import com.high.user.application.dto.response.InternalUserResponse;
import com.high.user.application.service.InternalUserService;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 내부 서비스 간 통신용 User API Controller
 * <p>
 * Order/Payment/Coupon Service에서 User 정보를 조회할 때 사용
 * Gateway에서 JWT 검증 후 X-User-Id, X-User-Role 헤더 추가
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
@PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
public class InternalController {

    private final InternalUserService internalUserService;

    /**
     * 단건 사용자 조회
     * <p>
     * GET /api/v1/internal/users/{userId}
     * </p>
     *
     * @param userId 사용자 고유 ID
     * @return 200 OK + ApiResponse<InternalUserResponse>
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<InternalUserResponse>> getUserById(
            @PathVariable UUID userId) {

        log.info("[Internal API] GET /api/v1/internal/users/{}", userId);
        InternalUserResponse response = internalUserService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 다건 사용자 조회 (Batch)
     * <p>
     * POST /api/v1/internal/users/batch
     * </p>
     *
     * @param request 사용자 ID 목록 (BatchUserRequest)
     * @return 200 OK + ApiResponse<List<InternalUserResponse>>
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<InternalUserResponse>>> getUsersByIds(
            @Valid @RequestBody BatchUserRequest request) {

        log.info("[Internal API] POST /api/v1/internal/users/batch - size: {}",
                request.userIds().size());
        List<InternalUserResponse> responses =
                internalUserService.getUsersByIds(request.userIds());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
