package com.high.order.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//PreAuthorize 예외 처리 (컨트롤러 예외)
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
        AccessDeniedException ex
    ) {
        log.error("[SecurityExceptionHandler] handleAccessDeniedException : {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", 1007);
        errorResponse.put("message", "접근 권한이 없습니다.");

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(errorResponse);
    }
}
