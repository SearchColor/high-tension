package com.high.gateway.controller;

import com.high.gateway.exception.GatewayErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Circuit Breaker Fallback Controller
 * Circuit Breaker가 OPEN 상태일 때 호출되는 Fallback 엔드포인트
 */
@Slf4j
@RestController
public class FallbackController {

    /**
     * User Service Fallback
     * User Service Circuit이 OPEN 상태일 때 호출됩니다.
     *
     * @param exchange ServerWebExchange
     * @return 503 Service Unavailable 응답
     */
    @RequestMapping("/fallback/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback(
            ServerWebExchange exchange) {

        // Circuit Breaker 예외 추출
        Throwable exception = exchange.getAttribute(
            ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);

        // 원본 요청 정보 추출
        String originalPath = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // 로깅
        log.warn("Circuit breaker fallback - Service: user-service, Path: {}, Method: {}",
                 originalPath, method);

        // Fallback 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", GatewayErrorCode.SERVICE_UNAVAILABLE.getCode());
        response.put("message", GatewayErrorCode.SERVICE_UNAVAILABLE.getMessage());

        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response));
    }
}
