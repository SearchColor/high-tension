package com.high.gateway.exception;

import com.library.module.exception.CustomException;
import lombok.Getter;

/**
 * Rate Limit Exceeded Exception
 *
 * 사용자가 요청 한도를 초과했을 때 발생하는 예외입니다.
 * 429 Too Many Requests 응답과 함께 Retry-After 헤더를 제공합니다.
 */
@Getter
public class RateLimitExceededException extends CustomException {

    /**
     * 재시도까지 대기 시간 (초)
     */
    private final int retryAfter;

    /**
     * @param retryAfter 재시도까지 대기 시간 (초)
     */
    public RateLimitExceededException(int retryAfter) {
        super(GatewayErrorCode.RATE_LIMIT_EXCEEDED);
        this.retryAfter = retryAfter;
    }
}