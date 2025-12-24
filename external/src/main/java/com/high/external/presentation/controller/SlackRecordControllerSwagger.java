package com.high.external.presentation.controller;

import com.high.external.application.dto.request.SendSlackMessageRequest;
import com.high.external.application.dto.response.SlackRecordDeleteDto;
import com.high.external.application.dto.response.SlackRecordDto;
import com.library.jpa.response.PageResponse;
import com.library.module.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Slack Record API", description = "Slack 메시지 기록 관리 API")
public interface SlackRecordControllerSwagger {

    @Operation(
            summary = "SlackRecord 단건 조회",
            description = "ID로 특정 Slack 메시지 기록을 조회합니다. (MASTER 권한 필요)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SlackRecord 조회 성공",
                    content = @Content(schema = @Schema(implementation = SlackRecordDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "SlackRecord 찾을 수 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<SlackRecordDto>> getSlackRecord(
            @Parameter(description = "SlackRecord ID", required = true)
            @PathVariable UUID id
    );

    @Operation(
            summary = "SlackRecord 목록 조회",
            description = "Slack 메시지 기록 목록을 페이징하여 조회합니다. (MASTER 권한 필요)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SlackRecord 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<PageResponse<SlackRecordDto>>> getSlackRecordList(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드", example = "createdAt")
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @Parameter(description = "오름차순 정렬 여부", example = "false")
            @RequestParam(value = "isAsc", defaultValue = "false") boolean isAsc
    );

    @Operation(
            summary = "SlackRecord 삭제",
            description = "특정 Slack 메시지 기록을 소프트 삭제합니다. (MASTER 권한 필요)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SlackRecord 삭제 성공",
                    content = @Content(schema = @Schema(implementation = SlackRecordDeleteDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "SlackRecord을 찾을 수 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<SlackRecordDeleteDto>> softDeleteSlackRecord(
            @Parameter(description = "삭제할 SlackRecord ID", required = true)
            @PathVariable UUID id
    );

    @Operation(
            summary = "Slack 메시지 전송 테스트",
            description = "지정된 수신자에게 Slack 메시지를 전송합니다. (MASTER 권한 필요)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Slack 메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<String>> sendSlackMessageTest(
            @Parameter(description = "Slack 메시지 전송 요청 정보", required = true)
            @RequestBody SendSlackMessageRequest request
    );

    @Operation(
            summary = "에러 로그 테스트",
            description = "Kibana 파이프라인 검증을 위한 테스트 에러 로그를 생성합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "테스트 에러 로그 생성 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    ResponseEntity<String> generateErrorLog();
}