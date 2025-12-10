package com.high.user.presentation;

import com.high.user.application.dto.request.UpdateRoleRequest;
import com.high.user.application.dto.response.UserSearchResponse;
import com.high.user.application.service.AdminService;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/master/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MASTER')")
public class AdminController {

    private final AdminService adminService;

    /**
     * 이메일로 사용자 검색
     * GET /api/v1/master/users/search?email={email}&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserSearchResponse>>> searchUsers(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("[ADMIN] Search users request: email={}", email);

        Page<UserSearchResponse> response = adminService.searchUsersByEmail(email, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자 권한 변경
     * PATCH /api/v1/master/users/{userId}/role
     */
    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserSearchResponse>> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("[ADMIN] Update user role request: userId={}, newRole={}", userId, request.role());

        UserSearchResponse response = adminService.updateUserRole(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}