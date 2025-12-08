package com.high.user.presentation;

import com.high.user.application.dto.request.*;
import com.high.user.application.dto.response.TokenResponse;
import com.high.user.application.dto.response.UserResponse;
import com.high.user.application.service.UserAuthService;
import com.high.user.application.service.UserService;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAuthService userAuthService;
    private final UserService userService;

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
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String accessToken = bearerToken.substring(7);

        log.info("Logout request received: userId={}", userId);

        userAuthService.logout(accessToken, userId.toString());

        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Get my info request received: userId={}", userId);

        UserResponse response = userService.getUserById(userId.toString());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserInfo(
            @Valid @RequestBody UpdateUserRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Update user info request received: userId={}", userId);

        UserResponse response = userService.updateUserInfo(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Change password request received: userId={}", userId);

        userService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @Valid @RequestBody DeleteUserRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Delete user request received: userId={}", userId);

        userService.deleteUser(userId, request);

        return ResponseEntity.noContent().build();
    }
}
