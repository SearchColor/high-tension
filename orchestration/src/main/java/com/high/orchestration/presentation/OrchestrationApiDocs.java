package com.high.orchestration.presentation;

import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.library.module.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Orchestration", description = "Saga 패턴 기반 오케스트레이션 API")
@RequestMapping("/api/v1/orchestrations")
public interface OrchestrationApiDocs {

    @Operation(summary = "주문 생성 API(Saga 패턴)", description = "Saga 패턴을 사용하여 주문을 생성합니다. 비동기로 처리되며 각 단계별로 보상 트랜잭션이 관리됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "주문 접수 성공 (비동기 처리 시작)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "권한 없음 (USER 권한 필요)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 오류 (Saga 시작 실패)",
            content = @Content
        )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/order")
    public ResponseEntity<ApiResponse<Void>> orderCreate(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "주문 생성 요청 정보 (상품, 수량, 배송지 등)",
            required = true,
            content = @Content(schema = @Schema(implementation = OrderCreateRequest.class))
        )
        @Valid @RequestBody OrderCreateRequest orderCreateRequest);

}
