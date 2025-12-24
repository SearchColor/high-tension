package com.high.user.presentation;

import com.high.user.application.dto.request.UpdateRoleRequest;
import com.high.user.application.dto.response.UserSearchResponse;
import com.high.user.application.service.AdminService;
import com.library.module.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin", description = "관리자 전용 사용자 관리 API (MASTER 권한 필요)")
@Slf4j
@RestController
@RequestMapping("/api/v1/master/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MASTER')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "사용자 검색", description = "이메일로 사용자를 검색합니다 (부분 일치, 페이징).",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER 권한 필요)")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserSearchResponse>>> searchUsers(
            @Parameter(description = "검색할 이메일 (부분 일치)", required = true, example = "user@example.com")
            @RequestParam String email,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("[ADMIN] Search users request: email={}", email);

        Page<UserSearchResponse> response = adminService.searchUsersByEmail(email, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "권한 변경", description = "사용자의 권한을 변경합니다 (USER, SELLER, MASTER).",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "권한 변경 성공",
            content = @Content(schema = @Schema(implementation = UserSearchResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserSearchResponse>> updateUserRole(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("[ADMIN] Update user role request: userId={}, newRole={}", userId, request.role());

        UserSearchResponse response = adminService.updateUserRole(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
