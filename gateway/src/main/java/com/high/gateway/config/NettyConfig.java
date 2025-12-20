package com.high.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Netty Configuration
 * Gateway의 HTTP 클라이언트 Connection Pool 설정
 */
@Configuration
public class NettyConfig {

    /**
     * ConnectionProvider Bean
     * Connection Pool 관리 및 타임아웃 설정
     *
     * Spring Cloud Gateway가 자동으로 이 Bean을 감지하여
     * HttpClient에 적용합니다.
     *
     * @return ConnectionProvider
     */
    @Bean
    public ConnectionProvider gatewayConnectionProvider() {
        return ConnectionProvider.builder("gateway-connection-pool")
            .maxConnections(500)                        // 최대 동시 연결 수
            .maxIdleTime(Duration.ofSeconds(30))       // 유휴 연결 유지 시간
            .maxLifeTime(Duration.ofSeconds(60))       // 최대 연결 수명
            .evictInBackground(Duration.ofSeconds(60)) // 백그라운드 연결 정리
            .build();
    }
}
