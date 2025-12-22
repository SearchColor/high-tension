package com.high.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Gateway Exception Handler
 * Gateway에서 발생하는 예외를 처리합니다.
 */
@Slf4j
@Order(-2)  // 기본 ErrorWebExceptionHandler보다 우선순위 높음
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // TimeoutException 처리
        if (ex instanceof TimeoutException) {
            return handleTimeoutException(exchange, ex);
        }

        // ConnectException 처리 (직접 예외 또는 cause로 포함된 경우)
        if (isConnectException(ex)) {
            return handleConnectTimeoutException(exchange, ex);
        }

        // ResponseStatusException 처리
        if (ex instanceof ResponseStatusException) {
            return handleResponseStatusException(exchange, (ResponseStatusException) ex);
        }

        // 기타 예외 처리
        return handleGenericException(exchange, ex);
    }

    /**
     * ConnectException 여부 확인
     * 직접 예외이거나 cause 체인에 포함된 경우 모두 감지
     */
    private boolean isConnectException(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            // 직접 ConnectException인 경우
            if (current instanceof java.net.ConnectException) {
                return true;
            }

            // UnknownHostException인 경우 (DNS 해석 실패)
            if (current instanceof java.net.UnknownHostException) {
                return true;
            }

            // Netty의 ConnectTimeoutException인 경우
            String exceptionName = current.getClass().getName();
            if (exceptionName.contains("ConnectTimeoutException")) {
                return true;
            }

            // 다음 cause 체크
            current = current.getCause();
        }

        return false;
    }

    /**
     * TimeoutException 처리
     * 응답 타임아웃 발생 시
     */
    private Mono<Void> handleTimeoutException(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway Timeout - Path: {}, Error: {}",
            exchange.getRequest().getURI().getPath(),
            ex.getMessage());

        return writeErrorResponse(
            exchange,
            HttpStatus.GATEWAY_TIMEOUT,
            GatewayErrorCode.GATEWAY_TIMEOUT.getCode(),
            GatewayErrorCode.GATEWAY_TIMEOUT.getMessage()
        );
    }

    /**
     * ConnectTimeoutException 처리
     * 연결 타임아웃 발생 시
     */
    private Mono<Void> handleConnectTimeoutException(ServerWebExchange exchange, Throwable ex) {
        log.error("Connect Timeout - Path: {}, Error: {}",
            exchange.getRequest().getURI().getPath(),
            ex.getMessage());

        return writeErrorResponse(
            exchange,
            HttpStatus.SERVICE_UNAVAILABLE,
            GatewayErrorCode.CONNECTION_TIMEOUT.getCode(),
            GatewayErrorCode.CONNECTION_TIMEOUT.getMessage()
        );
    }

    /**
     * ResponseStatusException 처리
     */
    private Mono<Void> handleResponseStatusException(ServerWebExchange exchange, ResponseStatusException ex) {
        log.error("Response Status Error - Path: {}, Status: {}, Reason: {}",
            exchange.getRequest().getURI().getPath(),
            ex.getStatusCode(),
            ex.getReason());

        return writeErrorResponse(
            exchange,
            (HttpStatus) ex.getStatusCode(),
            GatewayErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            ex.getReason() != null ? ex.getReason() : GatewayErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
    }

    /**
     * 일반 예외 처리
     */
    private Mono<Void> handleGenericException(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway Error - Path: {}, Error: {}",
            exchange.getRequest().getURI().getPath(),
            ex.getMessage(),
            ex);

        return writeErrorResponse(
            exchange,
            HttpStatus.INTERNAL_SERVER_ERROR,
            GatewayErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            GatewayErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
    }

    /**
     * 에러 응답 작성
     */
    private Mono<Void> writeErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            int code,
            String message) {

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format(
            "{\"success\":false,\"code\":%d,\"message\":\"%s\"}",
            code,
            message
        );

        DataBuffer buffer = exchange.getResponse()
            .bufferFactory()
            .wrap(errorJson.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
