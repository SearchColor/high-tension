package com.high.user.presentation;

import com.high.user.application.dto.request.PasskeyAuthenticationFinishRequest;
import com.high.user.application.dto.request.PasskeyAuthenticationStartRequest;
import com.high.user.application.dto.request.PasskeyRegistrationFinishRequest;
import com.high.user.application.dto.request.PasskeyRegistrationStartRequest;
import com.high.user.application.dto.request.PasskeyUpdateRequest;
import com.high.user.application.dto.response.*;
import com.high.user.application.service.PasskeyService;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/passkeys")
@RequiredArgsConstructor
public class PasskeyController {

    private final PasskeyService passkeyService;

    /**
     * 패스키 등록 시작
     * POST /api/v1/passkeys/register/start
     */
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PostMapping("/register/start")
    public ResponseEntity<ApiResponse<PasskeyRegistrationStartResponse>> startRegistration(
            @Valid @RequestBody(required = false) PasskeyRegistrationStartRequest request) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[POST] Passkey registration start: userId={}", userId);

        PasskeyRegistrationStartResponse response = passkeyService.startRegistration(
            userId, request != null ? request : new PasskeyRegistrationStartRequest(null));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 패스키 등록 완료
     * POST /api/v1/passkeys/register/finish
     */
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PostMapping("/register/finish")
    public ResponseEntity<ApiResponse<PasskeyRegistrationFinishResponse>> finishRegistration(
            @Valid @RequestBody PasskeyRegistrationFinishRequest request) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[POST] Passkey registration finish: userId={}", userId);

        PasskeyRegistrationFinishResponse response = passkeyService.finishRegistration(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 패스키 인증 시작 (로그인)
     * POST /api/v1/passkeys/authenticate/start
     */
    @PostMapping("/authenticate/start")
    public ResponseEntity<ApiResponse<PasskeyAuthenticationStartResponse>> startAuthentication(
            @Valid @RequestBody(required = false) PasskeyAuthenticationStartRequest request) {

        log.info("[POST] Passkey authentication start");

        PasskeyAuthenticationStartResponse response = passkeyService.startAuthentication(
            request != null ? request : new PasskeyAuthenticationStartRequest(null));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 패스키 인증 완료 (로그인 - JWT 발급)
     * POST /api/v1/passkeys/authenticate/finish
     */
    @PostMapping("/authenticate/finish")
    public ResponseEntity<ApiResponse<TokenResponse>> finishAuthentication(
            @Valid @RequestBody PasskeyAuthenticationFinishRequest request) {

        log.info("[POST] Passkey authentication finish");

        TokenResponse response = passkeyService.finishAuthentication(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 패스키 목록 조회
     * GET /api/v1/passkeys
     */
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PasskeyListResponse>>> getPasskeys() {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[GET] Get passkeys: userId={}", userId);

        List<PasskeyListResponse> response = passkeyService.getPasskeys(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 패스키 이름 수정
     * PATCH /api/v1/passkeys/{passkeyId}
     */
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/{passkeyId}")
    public ResponseEntity<ApiResponse<PasskeyListResponse>> updateCredentialName(
            @PathVariable UUID passkeyId,
            @Valid @RequestBody PasskeyUpdateRequest request) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[PATCH] Update passkey credential name: userId={}, passkeyId={}",
                 userId, passkeyId);

        PasskeyListResponse response = passkeyService.updateCredentialName(userId, passkeyId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 패스키 삭제
     * DELETE /api/v1/passkeys/{passkeyId}
     */
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @DeleteMapping("/{passkeyId}")
    public ResponseEntity<ApiResponse<Void>> deletePasskey(@PathVariable UUID passkeyId) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[DELETE] Delete passkey: userId={}, passkeyId={}", userId, passkeyId);

        passkeyService.deletePasskey(userId, passkeyId);

        return ResponseEntity.ok(ApiResponse.success("패스키가 삭제되었습니다."));
    }
}
