package com.high.orchestration.presentation;

import com.high.orchestration.application.OrderCreateSagaService;
import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SagaController implements OrchestrationApiDocs{

    private final OrderCreateSagaService orderCreateSagaService;

    @Override
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/order")
    public ResponseEntity<ApiResponse<Void>> orderCreate(@Valid @RequestBody OrderCreateRequest orderCreateRequest) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        orderCreateSagaService.startOrderCreateStage(orderCreateRequest, userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("주문이 접수되었습니다."));
    }

}
