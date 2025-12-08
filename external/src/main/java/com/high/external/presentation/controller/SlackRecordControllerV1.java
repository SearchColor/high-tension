package com.high.external.presentation.controller;


import com.high.external.application.dto.request.SendSlackMessageRequest;
import com.high.external.application.dto.response.SlackRecordDeleteDto;
import com.high.external.application.dto.response.SlackRecordDto;
import com.high.external.application.service.SlackMessageServiceV1;
import com.high.external.application.service.SlackRecordServiceV1;
import com.library.jpa.response.PageResponse;
import com.library.module.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/externals/slacks")
@RequiredArgsConstructor
public class SlackRecordControllerV1 {

    private final SlackRecordServiceV1 serviceV1;
    private final SlackMessageServiceV1 messageServiceV1;


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlackRecordDto>> getSlackRecord(
            @PathVariable UUID id
            ){
        return ResponseEntity.ok(ApiResponse.success(serviceV1.getSlackRecord(id)));
    }

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


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<SlackRecordDeleteDto>> softDeleteSlackRecord(
            @PathVariable UUID id,
            @RequestHeader("x-user-id") UUID userId
            ){
        SlackRecordDeleteDto response = serviceV1.softDeleteSlackRecord(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<String>> sendSlackMessageTest(
            @RequestBody SendSlackMessageRequest request
            ){
        String response = messageServiceV1.slackMessageSend(request.getRecipientId(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
