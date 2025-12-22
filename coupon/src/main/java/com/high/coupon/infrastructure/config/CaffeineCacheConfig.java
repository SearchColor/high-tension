package com.high.coupon.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 쿠폰 정보 캐싱은 Local
 * 특정 기능에 따라 캐싱 옵션이 달라질 수 있음 -> 이 때는 enum으로 분리하는 방법 존재
 */
@Configuration
public class CaffeineCacheConfig {

    @Bean
    public CacheManager localCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60)) // 60초가 지나면 만료 todo LocalCache에서 TTL을 길게 잡으면 위험할까?
                        .maximumSize(100) // 100개
                );
        return cacheManager;
    }
}
