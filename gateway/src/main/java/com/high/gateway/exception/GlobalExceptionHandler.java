package com.high.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.module.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for Gateway (WebFlux)
 * CustomException 기반 예외 처리 (common-module 패턴 준수)
 *
 * Note: Gateway는 WebFlux이므로 ErrorWebExceptionHandler를 직접 구현
 * (common-module의 @RestControllerAdvice는 Spring MVC용)
 */
@Slf4j
@Order(-1)
@Configuration
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);

        HttpStatus status;
        int code;
        String message;

        // CustomException 처리 (common-module 패턴)
        if (ex instanceof CustomException) {
            CustomException customException = (CustomException) ex;
            status = customException.getBaseErrorCode().getStatus();
            code = customException.getBaseErrorCode().getCode();
            message = customException.getBaseErrorCode().getMessage();
        } else {
            // 기타 예외는 500 처리
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = 5000;
            message = "서버 내부 오류가 발생했습니다";
        }

        return writeErrorResponse(exchange, status, code, message);
    }

    /**
     * 에러 응답 작성 (common-module 응답 형식 준수)
     * {
     *   "success": false,
     *   "code": 9000,
     *   "message": "유효하지 않은 토큰입니다"
     * }
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, int code, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", code);
        errorResponse.put("message", message);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing error response: {}", e.getMessage());
            return exchange.getResponse().setComplete();
        }
    }
}