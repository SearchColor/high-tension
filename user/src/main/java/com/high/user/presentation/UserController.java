package com.high.user.presentation;

import com.high.user.application.dto.request.LoginRequest;
import com.high.user.application.dto.request.SignupRequest;
import com.high.user.application.dto.response.TokenResponse;
import com.high.user.application.dto.response.UserResponse;
import com.high.user.application.service.UserAuthService;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
        @Valid @RequestBody SignupRequest request
    ) {
        log.info("Signup request received: email={}", request.email());

        UserResponse response = userAuthService.signup(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request received: email={}", request.email());

        TokenResponse response = userAuthService.login(request);

        return ResponseEntity
            .ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // SecurityContext에서 userId 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        log.info("Logout request received: userId={}", userId);

        userAuthService.logout(userId);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
