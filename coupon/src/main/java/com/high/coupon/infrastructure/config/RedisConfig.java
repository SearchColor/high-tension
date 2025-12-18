package com.high.coupon.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisConfig {

    @Bean
    public RedisScript<Long> issueCouponScript(){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

        // resource 폴더 안 lua 파일 경로
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/issue_coupon.lua")));

        // 반환 타입 설정 (Lua 숫자 리턴 = Long)
        redisScript.setResultType(Long.class);

        return redisScript;
    }
}
