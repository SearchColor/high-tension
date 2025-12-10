package com.high.user.infrastructure.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.user.domain.exception.UserErrorCode;
import com.library.module.common.handler.GlobalExceptionHandler;
import com.library.module.response.ApiResponse;
import com.library.module.response.ErrorResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * User Service 전용 예외 처리 핸들러
 * common-module의 GlobalExceptionHandler를 상속받아 FeignException 처리 추가
 */
@Slf4j
@RestControllerAdvice
public class UserGlobalExceptionHandler extends GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * FeignException 처리 - Coupon Service의 에러를 투명하게 전달
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<?>> handleFeignException(FeignException ex) {
        log.error("FeignException occurred: status={}, message={}", ex.status(), ex.getMessage());

        try {
            // Feign Response Body에서 Coupon Service의 ErrorResponse 파싱
            if (ex.responseBody().isPresent()) {
                ByteBuffer buffer = ex.responseBody().get();
                String responseBody = StandardCharsets.UTF_8.decode(buffer).toString();

                // Coupon Service의 ErrorResponse 파싱 시도
                ApiResponse<?> couponErrorResponse = objectMapper.readValue(responseBody, ApiResponse.class);

                log.info("Forwarding Coupon Service error: {}", responseBody);

                return new ResponseEntity<>(
                        couponErrorResponse,
                        HttpStatus.valueOf(ex.status())
                );
            }
        } catch (Exception e) {
            log.warn("Failed to parse Coupon Service error response, using fallback", e);
        }

        // 파싱 실패 시 기본 에러 응답
        ErrorResponse errorResponse = new ErrorResponse(
                ex.status(),
                "외부 서비스 에러: " + ex.getMessage()
        );

        return new ResponseEntity<>(
                ApiResponse.error(errorResponse),
                HttpStatus.valueOf(ex.status())
        );
    }

    /**
     * @Valid 검증 실패 처리 - User Service 커스텀 에러 코드 매핑
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>>
    handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        // 첫 번째 검증 실패 필드 기준으로 에러 코드 결정
        FieldError fieldError = ex.getBindingResult().getFieldError();

        if (fieldError == null) {
            ErrorResponse errorResponse = new ErrorResponse(1001, "입력 값이 올바르지 않습니다");
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(errorResponse));
        }

        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage();

        // 필드명에 따라 User Service 에러 코드 매핑
        UserErrorCode errorCode = mapFieldToErrorCode(field, message);

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getCode(),
                message != null ? message : errorCode.getMessage()
        );

        log.info("Validation error mapped - field: {}, code: {}, message: {}",
                field, errorCode.getCode(), message);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Spring Security AccessDeniedException 처리 (403 Forbidden)
     * @PreAuthorize 검증 실패 시 호출됨
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(9101, "접근 권한이 없습니다");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * 필드명과 메시지를 기반으로 User Service 에러 코드 매핑
     */
    private UserErrorCode mapFieldToErrorCode(String field, String message) {
        return switch (field) {
            case "email" -> UserErrorCode.INVALID_EMAIL_FORMAT;      // 2100
            case "password" -> UserErrorCode.INVALID_PASSWORD;       // 2101
            case "name" -> UserErrorCode.INVALID_NAME;               // 2103
            case "phoneNumber" -> UserErrorCode.INVALID_PHONE_NUMBER; // 2104
            default -> {
                log.warn("Unmapped validation field: {}, using default error code",
                        field);
                yield UserErrorCode.INVALID_EMAIL_FORMAT;
            }
        };
    }
}
