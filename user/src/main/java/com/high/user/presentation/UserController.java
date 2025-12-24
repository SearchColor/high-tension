package com.high.user.presentation;

import com.high.user.application.dto.request.*;
import com.high.user.application.dto.response.CouponResponse;
import com.high.user.application.dto.response.TokenResponse;
import com.high.user.application.dto.response.UserResponse;
import com.high.user.application.service.UserAuthService;
import com.high.user.application.service.UserCouponService;
import com.high.user.application.service.UserService;
import com.library.security.util.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User", description = "사용자 인증 및 정보 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAuthService userAuthService;
    private final UserService userService;
    private final UserCouponService userCouponService;

    @Operation(summary = "회원가입", description = "신규 사용자를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<com.library.module.response.ApiResponse<UserResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        log.info("Signup request received: email={}", request.email());

        UserResponse response = userAuthService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)")
    })
    @PostMapping("/login")
    public ResponseEntity<com.library.module.response.ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request received: email={}", request.email());

        TokenResponse response = userAuthService.login(request);

        return ResponseEntity
                .ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/reissue")
    public ResponseEntity<com.library.module.response.ApiResponse<TokenResponse>> reissueToken(
            @Valid @RequestBody TokenReissueRequest request) {
        log.info("Token reissue request received");

        TokenResponse response = userAuthService.reissueToken(request);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자를 로그아웃하고 토큰을 무효화합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PostMapping("/logout")
    public ResponseEntity<com.library.module.response.ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String bearerToken) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String accessToken = bearerToken.substring(7);

        log.info("Logout request received: userId={}", userId);

        userAuthService.logout(accessToken, userId.toString());

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success("로그아웃 되었습니다."));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping("/me")
    public ResponseEntity<com.library.module.response.ApiResponse<UserResponse>> getMyInfo() {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Get my info request received: userId={}", userId);

        UserResponse response = userService.getUserById(userId.toString());

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "회원정보 수정", description = "이름, 전화번호, 주소를 수정합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PutMapping("/me")
    public ResponseEntity<com.library.module.response.ApiResponse<UserResponse>> updateUserInfo(
            @Valid @RequestBody UpdateUserRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Update user info request received: userId={}", userId);

        UserResponse response = userService.updateUserInfo(userId, request);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음 또는 현재 비밀번호 불일치"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/me/password")
    public ResponseEntity<com.library.module.response.ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Change password request received: userId={}", userId);

        userService.changePassword(userId, request);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(null));
    }

    @Operation(summary = "회원 탈퇴", description = "비밀번호를 확인하고 회원을 탈퇴합니다 (Soft Delete).",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음 또는 비밀번호 불일치"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @Valid @RequestBody DeleteUserRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Delete user request received: userId={}", userId);

        userService.deleteUser(userId, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 쿠폰 조회", description = "현재 로그인한 사용자가 보유한 쿠폰 목록을 조회합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping("/me/coupons")
    public ResponseEntity<com.library.module.response.ApiResponse<List<CouponResponse>>> getMyCoupons() {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        log.info("Get my coupons request received: userId={}", userId);

        List<CouponResponse> response = userCouponService.getUserCoupons(userId);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }
}
