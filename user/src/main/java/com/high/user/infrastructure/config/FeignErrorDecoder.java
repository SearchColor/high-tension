package com.high.user.infrastructure.config;

import com.high.user.domain.exception.CouponServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign error - methodKey: {}, status: {}", methodKey, response.status());

        // 4xx: Coupon Service의 비즈니스 에러 → FeignException으로 전달 (원본 응답 포함)
        // FeignException은 status와 response body를 포함하므로 GlobalExceptionHandler에서 처리 가능
        if (response.status() >= 400 && response.status() < 500) {
            log.warn("Coupon Service business error: status={}", response.status());
            return defaultDecoder.decode(methodKey, response);
        }

        // 5xx 또는 통신 장애: User Service의 CouponServiceException 사용
        log.error("Coupon Service unavailable: status={}", response.status());
        return new CouponServiceException();
    }
}