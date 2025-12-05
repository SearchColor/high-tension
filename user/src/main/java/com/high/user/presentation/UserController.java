package com.high.user.presentation;

import com.high.user.application.dto.request.*;
import com.high.user.application.dto.response.TokenResponse;
import com.high.user.application.dto.response.UserResponse;
import com.high.user.application.service.UserAuthService;
import com.high.user.application.service.UserCommandService;
import com.high.user.application.service.UserQueryService;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAuthService userAuthService;
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        log.info("Signup request received: email={}", request.email());

        UserResponse response = userAuthService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request received: email={}", request.email());

        TokenResponse response = userAuthService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String bearerToken) {
        // JWT 인증 정보에서 userId 추출
        // SecurityContext → Authentication → Principal(userId)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // Bearer 토큰에서 실제 JWT 추출 ("Bearer " prefix 제거)
        String accessToken = bearerToken.substring(7);

        log.info("Logout request received: userId={}", userId);

        userAuthService.logout(accessToken, userId);

        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        // JWT 인증 정보에서 userId 추출
        // Gateway의 JwtAuthenticationGlobalFilter에서 검증된 정보
        // SecurityContext → Authentication → Principal(userId)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        log.info("Get my info request received: userId={}", userId);

        UserResponse response = userQueryService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserInfo(
            @Valid @RequestBody UpdateUserRequest request) {
        // JWT 인증 정보에서 userId 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        java.util.UUID userId = java.util.UUID.fromString(userIdStr);

        log.info("Update user info request received: userId={}", userId);

        UserResponse response = userCommandService.updateUserInfo(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        // JWT 인증 정보에서 userId 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        java.util.UUID userId = java.util.UUID.fromString(userIdStr);

        log.info("Change password request received: userId={}", userId);

        userCommandService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @Valid @RequestBody DeleteUserRequest request) {
        // JWT 인증 정보에서 userId 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        java.util.UUID userId = java.util.UUID.fromString(userIdStr);

        log.info("Delete user request received: userId={}", userId);

        userCommandService.deleteUser(userId, request);

        return ResponseEntity.noContent().build();
    }
}
