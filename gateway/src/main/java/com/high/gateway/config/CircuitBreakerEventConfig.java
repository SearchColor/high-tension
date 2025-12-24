package com.high.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Circuit Breaker Event Config
 * Circuit Breaker 상태 변화 이벤트를 로깅합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class
CircuitBreakerEventConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Circuit Breaker 이벤트 리스너 등록
     * 애플리케이션 시작 시 모든 Circuit Breaker에 이벤트 리스너를 등록합니다.
     */
    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers()
            .forEach(this::addEventConsumer);
    }

    /**
     * Circuit Breaker 이벤트 소비자 추가
     *
     * @param circuitBreaker Circuit Breaker 인스턴스
     */
    private void addEventConsumer(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
            // 상태 전환 이벤트 (CLOSED -> OPEN, OPEN -> HALF_OPEN, HALF_OPEN -> CLOSED)
            .onStateTransition(event -> {
                String toState = event.getStateTransition().getToState().name();
                log.warn("[CIRCUIT BREAKER] {} -> {}",
                    circuitBreaker.getName(), toState);
            })
            // 에러 발생 이벤트
            .onError(event -> log.warn("[CIRCUIT BREAKER] {} error: {}",
                circuitBreaker.getName(), event.getThrowable().getMessage()));
    }
}
