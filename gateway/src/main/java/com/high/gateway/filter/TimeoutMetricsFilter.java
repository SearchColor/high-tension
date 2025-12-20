package com.high.gateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Timeout Metrics Filter
 * 요청 처리 시간을 측정하고 타임아웃 발생 시 메트릭을 기록합니다.
 */
@Slf4j
@Component
public class TimeoutMetricsFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_START_TIME = "requestStartTime";
    private final Timer requestTimer;
    private final Counter timeoutCounter;

    public TimeoutMetricsFilter(MeterRegistry meterRegistry) {
        // 요청 처리 시간 측정
        this.requestTimer = Timer.builder("gateway.request.duration")
            .description("Gateway request duration")
            .register(meterRegistry);

        // 타임아웃 발생 카운터
        this.timeoutCounter = Counter.builder("gateway.request.timeout")
            .description("Gateway request timeout count")
            .register(meterRegistry);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 요청 시작 시간 기록
        exchange.getAttributes().put(REQUEST_START_TIME, System.currentTimeMillis());

        // 2. 요청 처리
        return chain.filter(exchange)
            .doOnSuccess(aVoid -> recordMetrics(exchange, null))
            .doOnError(throwable -> recordMetrics(exchange, throwable))
            .onErrorResume(Mono::error);  // 에러를 다시 던짐 (GatewayExceptionHandler가 처리)
    }

    /**
     * 메트릭 기록
     */
    private void recordMetrics(ServerWebExchange exchange, Throwable throwable) {
        Long startTime = exchange.getAttribute(REQUEST_START_TIME);
        if (startTime == null) {
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        String path = exchange.getRequest().getURI().getPath();
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();

        // 요청 처리 시간 기록
        requestTimer.record(Duration.ofMillis(duration));

        // 타임아웃 발생 시
        if (throwable instanceof TimeoutException ||
            (statusCode != null && statusCode.value() == HttpStatus.GATEWAY_TIMEOUT.value())) {

            timeoutCounter.increment();

            log.warn("Timeout occurred - Path: {}, Duration: {}ms",
                path, duration);
        }

        // 느린 요청 경고 (3초 이상)
        if (duration > 3000) {
            log.warn("Slow request detected - Path: {}, Duration: {}ms",
                path, duration);
        }
    }

    @Override
    public int getOrder() {
        return -3;  // GatewayExceptionHandler(-2)보다 먼저 실행
    }
}
