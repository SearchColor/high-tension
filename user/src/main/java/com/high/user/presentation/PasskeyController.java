package com.high.user.presentation;

import com.high.user.application.dto.request.PasskeyAuthenticationFinishRequest;
import com.high.user.application.dto.request.PasskeyAuthenticationStartRequest;
import com.high.user.application.dto.request.PasskeyRegistrationFinishRequest;
import com.high.user.application.dto.request.PasskeyRegistrationStartRequest;
import com.high.user.application.dto.request.PasskeyUpdateRequest;
import com.high.user.application.dto.response.*;
import com.high.user.application.service.PasskeyService;
import com.library.security.util.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Passkey", description = "WebAuthn (FIDO2) 패스키 인증 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/passkeys")
@RequiredArgsConstructor
public class PasskeyController {

    private final PasskeyService passkeyService;

    @Operation(summary = "패스키 등록 시작", description = "WebAuthn 패스키 등록 프로세스를 시작하고 Challenge를 생성합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 시작 성공 (Challenge 반환)"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PostMapping("/register/start")
    public ResponseEntity<com.library.module.response.ApiResponse<PasskeyRegistrationStartResponse>> startRegistration(
            @Valid @RequestBody(required = false) PasskeyRegistrationStartRequest request) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[POST] Passkey registration start: userId={}", userId);

        PasskeyRegistrationStartResponse response = passkeyService.startRegistration(
            userId, request != null ? request : new PasskeyRegistrationStartRequest(null));

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "패스키 등록 완료", description = "WebAuthn 패스키 등록을 완료하고 Credential을 저장합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "패스키 등록 완료"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 인증 응답"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PostMapping("/register/finish")
    public ResponseEntity<com.library.module.response.ApiResponse<PasskeyRegistrationFinishResponse>> finishRegistration(
            @Valid @RequestBody PasskeyRegistrationFinishRequest request) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[POST] Passkey registration finish: userId={}", userId);

        PasskeyRegistrationFinishResponse response = passkeyService.finishRegistration(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "패스키 인증 시작", description = "WebAuthn 패스키 로그인 프로세스를 시작하고 Challenge를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 시작 성공 (Challenge 반환)")
    })
    @PostMapping("/authenticate/start")
    public ResponseEntity<com.library.module.response.ApiResponse<PasskeyAuthenticationStartResponse>> startAuthentication(
            @Valid @RequestBody(required = false) PasskeyAuthenticationStartRequest request) {

        log.info("[POST] Passkey authentication start");

        PasskeyAuthenticationStartResponse response = passkeyService.startAuthentication(
            request != null ? request : new PasskeyAuthenticationStartRequest(null));

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "패스키 인증 완료", description = "WebAuthn 패스키 인증을 완료하고 JWT 토큰을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 성공 (JWT 토큰 반환)",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 인증 응답"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/authenticate/finish")
    public ResponseEntity<com.library.module.response.ApiResponse<TokenResponse>> finishAuthentication(
            @Valid @RequestBody PasskeyAuthenticationFinishRequest request) {

        log.info("[POST] Passkey authentication finish");

        TokenResponse response = passkeyService.finishAuthentication(request);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "내 패스키 목록 조회", description = "현재 로그인한 사용자가 등록한 모든 패스키 목록을 조회합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping
    public ResponseEntity<com.library.module.response.ApiResponse<List<PasskeyListResponse>>> getPasskeys() {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[GET] Get passkeys: userId={}", userId);

        List<PasskeyListResponse> response = passkeyService.getPasskeys(userId);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "패스키 이름 수정", description = "등록된 패스키의 이름을 변경합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "패스키를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/{passkeyId}")
    public ResponseEntity<com.library.module.response.ApiResponse<PasskeyListResponse>> updateCredentialName(
            @Parameter(description = "패스키 ID", required = true)
            @PathVariable UUID passkeyId,
            @Valid @RequestBody PasskeyUpdateRequest request) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[PATCH] Update passkey credential name: userId={}, passkeyId={}",
                 userId, passkeyId);

        PasskeyListResponse response = passkeyService.updateCredentialName(userId, passkeyId, request);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success(response));
    }

    @Operation(summary = "패스키 삭제", description = "등록된 패스키를 삭제합니다.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "패스키를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @DeleteMapping("/{passkeyId}")
    public ResponseEntity<com.library.module.response.ApiResponse<Void>> deletePasskey(
            @Parameter(description = "패스키 ID", required = true)
            @PathVariable UUID passkeyId) {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        log.info("[DELETE] Delete passkey: userId={}, passkeyId={}", userId, passkeyId);

        passkeyService.deletePasskey(userId, passkeyId);

        return ResponseEntity.ok(com.library.module.response.ApiResponse.success("패스키가 삭제되었습니다."));
    }
}
