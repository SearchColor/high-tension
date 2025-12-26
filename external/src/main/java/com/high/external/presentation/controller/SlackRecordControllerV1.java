package com.high.external.presentation.controller;


import com.high.external.application.dto.request.SendSlackMessageRequest;
import com.high.external.application.dto.response.SlackRecordDeleteDto;
import com.high.external.application.dto.response.SlackRecordDto;
import com.high.external.application.service.SlackMessageServiceV1;
import com.high.external.application.service.SlackRecordServiceV1;
import com.library.jpa.response.PageResponse;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/externals/slacks")
@RequiredArgsConstructor
public class SlackRecordControllerV1 implements SlackRecordControllerSwagger{

    private final SlackRecordServiceV1 serviceV1;
    private final SlackMessageServiceV1 messageServiceV1;
    private static final Logger logger = LoggerFactory.getLogger(SlackRecordControllerV1.class);


    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlackRecordDto>> getSlackRecord(
            @PathVariable UUID id
            ){
        return ResponseEntity.ok(ApiResponse.success(serviceV1.getSlackRecord(id)));
    }

    @PreAuthorize("hasRole('MASTER')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SlackRecordDto>>> getSlackRecordList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "false") boolean isAsc
            ) {
        Page<SlackRecordDto> slackRecordDtoPage = serviceV1.getSlackRecordList(page, size, sortBy, isAsc);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(slackRecordDtoPage)));
    }

    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<SlackRecordDeleteDto>> softDeleteSlackRecord(
            @PathVariable UUID id
            ){
        UUID userId = SecurityContextUtil.getCurrentUserId();
        SlackRecordDeleteDto response = serviceV1.softDeleteSlackRecord(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('MASTER')")
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<String>> sendSlackMessageTest(
            @RequestBody SendSlackMessageRequest request
            ){
        String response = messageServiceV1.slackMessageSend(request.getRecipientId(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @GetMapping("/test/error")
    public ResponseEntity<String> generateErrorLog() {
        // 🚨 의도적으로 ERROR 레벨 로그를 발생시킵니다.
        logger.error("!!! TEST_ERROR_START_DUMP !!! - This is a manual test error log to verify Kibana pipeline. Time: {}", System.currentTimeMillis());

        return ResponseEntity.ok("Test Error log sent.");
    }


}
