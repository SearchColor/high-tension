package com.high.orchestration.presentation;

import com.high.orchestration.application.OrderCreateSagaService;
import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orchestration")
public class SagaController {

    private final OrderCreateSagaService orderCreateSagaService;



    @PostMapping("/order")
    public ResponseEntity<ApiResponse<Void>> orderCreate(@Valid @RequestBody OrderCreateRequest orderCreateRequest) {
        //인증인가 적용되면 헤더에서 주문자 ID 추출
        UUID ordererId = UUID.randomUUID();
        orderCreateSagaService.startOrderCreateStage(orderCreateRequest, ordererId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("주문이 접수되었습니다."));
    }

}
