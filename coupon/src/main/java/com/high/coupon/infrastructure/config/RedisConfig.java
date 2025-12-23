package com.high.coupon.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Redis 전용 ObjectMapper
     * - LocalDateTime 지원
     * - @class 미포함
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    /**
     * Redis value Serializer
     * ObjectMapper 주입
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> redisSerializer(
            ObjectMapper redisObjectMapper
    ) {
        return new Jackson2JsonRedisSerializer<>(
                redisObjectMapper,
                Object.class
        );
    }

    /**
     * RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            Jackson2JsonRedisSerializer<Object> redisValueSerializer){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key -> String
        template.setKeySerializer(new StringRedisSerializer());
        // Value -> Json (Redis 데이터 Json으로 저장)
        template.setValueSerializer(redisValueSerializer);

        // Hash Key / Value
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(redisValueSerializer);

        return template;
    }

//
//    /**
//     * CacheManager
//     * Key - String, Value - Json
//     * 기본 TTL 3일 (todo 테스트 중은 더 짧게)
//     * todo 필요 시 커스텀 추가
//     */
//    @Bean
//    public RedisCacheManager redisCacheManager(
//            RedisConnectionFactory connectionFactory,
//            Jackson2JsonRedisSerializer<Object> redisValueSerializer){
//
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofDays(3)) // 쿠폰 정보 3일 뒤 만료
//                .serializeKeysWith(RedisSerializationContext.SerializationPair
//                        .fromSerializer(new StringRedisSerializer())
//                )
//                .serializeValuesWith(RedisSerializationContext.SerializationPair
//                        .fromSerializer(redisValueSerializer)
//                );
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(config)
//                .build();
//    }


    /**
     * 쿠폰 발급 Lua
     */
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
