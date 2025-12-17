package com.high.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 요청 시작 시간 기록
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        // 2. 응답이 완료된 후 실행될 로직 (Post Filter)
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            ServerHttpResponse response = exchange.getResponse();

            // 3. 기존 서비스들과 통일된 구조로 데이터 정리
            // Logback JSON Appender가 이를 잡아서 파일로 저장하게 됩니다.
            // 아래 로그는 Slf4j의 MDC나 별도 Map을 사용해 JSON으로 변환되도록 설정되었다고 가정합니다.
            log.info("Gateway Access Log",
                    StructuredArguments.kv("http.method", request.getMethod().name()),
                    StructuredArguments.kv("url.path", request.getPath().value()),
                    StructuredArguments.kv("duration_ms", duration),
                    StructuredArguments.kv("http.status_code", response.getStatusCode().value()),
                    StructuredArguments.kv("log.type", "gateway_access")
            );
        }));
    }

    @Override
    public int getOrder() {
        // 필터 우선순위 설정 (가장 높은 우선순위로 설정하여 전체 시간을 측정)
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
